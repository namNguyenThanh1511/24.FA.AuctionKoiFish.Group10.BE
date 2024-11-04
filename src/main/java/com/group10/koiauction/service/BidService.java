package com.group10.koiauction.service;

import com.group10.koiauction.constant.ServiceFeePercent;
import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.exception.BidException;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.BidMapper;
import com.group10.koiauction.repository.BidRepository;
import com.group10.koiauction.model.request.BidRequestDTO;
import com.group10.koiauction.model.request.BuyNowRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponseAccountDTO;
import com.group10.koiauction.model.response.BidResponseDTO;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private AuctionSessionRepository auctionSessionRepository;

    @Autowired
    private AccountUtils accountUtils;

    @Autowired
    private KoiFishRepository koiFishRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BidMapper bidMapper;

    @Autowired
    private AuctionSessionService auctionSessionService;

    public BidResponseDTO createBid(BidRequestDTO bidRequestDTO) {
        Account memberAccount = accountUtils.getCurrentAccount();
        AuctionSession auctionSession = getAuctionSessionByID(bidRequestDTO.getAuctionSessionId());

        if (auctionSession.getStatus() == AuctionSessionStatus.COMPLETED) {
            throw new BidException("This auction session has already ended.");
        }

        if (auctionSession.getAuctionType() == AuctionSessionType.FIXED_PRICE) {
            // Logic đấu giá cố định (fixed price)
            return handleFixedPrice(bidRequestDTO, auctionSession, memberAccount);
        } else if (auctionSession.getAuctionType() == AuctionSessionType.ASCENDING) {
            // Logic đấu giá tăng dần (ascending)
            return handleAscendingBid(bidRequestDTO, auctionSession, memberAccount);
        } else {
            throw new BidException("Unsupported auction type.");
        }
    }

    private BidResponseDTO handleFixedPrice(BidRequestDTO bidRequestDTO, AuctionSession auctionSession, Account memberAccount) {
        boolean hasUserAlreadyBid = bidRepository.existsByMemberAndAuctionSession(memberAccount, auctionSession);
        if (hasUserAlreadyBid) {
            throw new BidException("You can only bid once in this auction session.");
        }

        double bidAmount = auctionSession.getStartingPrice();

        if (bidRequestDTO.getBidAmount() > bidAmount || bidRequestDTO.getBidAmount() < bidAmount) {
            throw new BidException("Your bid amount cannot exceed the starting price for a fixed price auction.");
        }

        if (memberAccount.getBalance() < auctionSession.getMinBalanceToJoin()) {
            throw new BidException("Your balance does not have enough money to join the auction.");
        }

        memberAccount.setBalance(memberAccount.getBalance() - bidAmount);

        Bid bid = new Bid();
        bid.setBidAt(new Date());
        bid.setBidAmount(auctionSession.getStartingPrice());
        bid.setAuctionSession(auctionSession);
        bid.setMember(memberAccount);

        updateAuctionSessionCurrentPrice(bidAmount, auctionSession);

        Transaction transaction = new Transaction();
        transaction.setCreateAt(new Date());
        transaction.setFrom(memberAccount);
        transaction.setType(TransactionEnum.BID);
        transaction.setAmount(auctionSession.getStartingPrice());
        transaction.setDescription("Bidding (-) " + bidAmount);
        transaction.setBid(bid);
        transaction.setAuctionSession(auctionSession);
        transaction.setStatus(TransactionStatus.SUCCESS);
        bid.setTransaction(transaction);

        try {
            bid = bidRepository.save(bid);
            transactionRepository.save(transaction);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

        BidResponseDTO bidResponseDTO = bidMapper.toBidResponseDTO(bid);
        AuctionSessionResponseAccountDTO memberResponse = new AuctionSessionResponseAccountDTO();
        memberResponse.setId(memberAccount.getUser_id());
        memberResponse.setUsername(memberAccount.getUsername());
        memberResponse.setFullName(memberAccount.getFirstName() + " " + memberAccount.getLastName());
        bidResponseDTO.setMember(memberResponse);
        bidResponseDTO.setAuctionSessionId(auctionSession.getAuctionSessionId());

        return bidResponseDTO;
    }

    private BidResponseDTO handleAscendingBid(BidRequestDTO bidRequestDTO, AuctionSession auctionSession, Account memberAccount) {
        double bidRequestAmountIncrement = bidRequestDTO.getBidAmount();
        double previousMaxBidAmount = findMaxBidAmount(auctionSession.getBidSet());

        if (previousMaxBidAmount == 0) {
            previousMaxBidAmount = auctionSession.getCurrentPrice();
        }
        //Lấy bid gần nhất của member hiện tại (người đang đấu giá ) để tính lượng tiêu hao trong ví
        double currentBidAmount = previousMaxBidAmount + bidRequestAmountIncrement;
        Bid latestBidOfCurrentMember =
                bidRepository.getLatestBidAmountOfCurrentMemberOfAuctionSession(memberAccount.getUser_id(),
                        auctionSession.getAuctionSessionId());
        double memberLostAmount;

        if (latestBidOfCurrentMember != null) {// nếu member đã có đấu giá trong phiên này
            memberLostAmount = currentBidAmount - latestBidOfCurrentMember.getBidAmount();
        } else {// lần đầu tiên member  đấu giá trong phiên này
            memberLostAmount = auctionSession.getCurrentPrice() + bidRequestAmountIncrement;
        }
        if (memberAccount.getBalance() < auctionSession.getMinBalanceToJoin()) {
            throw new BidException("Your balance does not have enough money to join the auction." + "You currently " +
                    "have  " + memberAccount.getBalance() + " but required : " + auctionSession.getMinBalanceToJoin());
        }
        if (bidRequestDTO.getBidAmount() > memberAccount.getBalance()) {
            throw new BidException("Your account does not have enough money to bid this amount ");
        }
        if (currentBidAmount < previousMaxBidAmount + auctionSession.getBidIncrement()) {
            throw new BidException("Your bid is lower than the required minimum increment.");
        }

        if (memberLostAmount > memberAccount.getBalance()) {
            throw new BidException("Your account does not have enough money to bid ! " + "You currently have : " + memberAccount.getBalance() + " But required " + memberLostAmount);
        }

        if (bidRequestDTO.getBidAmount() >= auctionSession.getBuyNowPrice()) {//khi đấu giá vượt quá Buy Now ->
            // chuyển sang buy now , ko tính là bid nữa
            throw new RuntimeException("You can buy now this fish");
        }

        Bid bid = new Bid();
        bid.setBidAt(new Date());
        bid.setBidAmount(currentBidAmount);
        bid.setAuctionSession(getAuctionSessionByID(bidRequestDTO.getAuctionSessionId()));
        bid.setMember(memberAccount);
        updateAuctionSessionCurrentPrice(currentBidAmount, auctionSession);

        Transaction transaction = new Transaction();
        transaction.setCreateAt(new Date());
        transaction.setFrom(memberAccount);
        transaction.setType(TransactionEnum.BID);
        transaction.setAmount(bidRequestDTO.getBidAmount());
        transaction.setDescription("Bidding (-)  " + memberLostAmount);

        transaction.setBid(bid);
        transaction.setAuctionSession(auctionSession);
        transaction.setStatus(TransactionStatus.SUCCESS);
        bid.setTransaction(transaction);
        memberAccount.setBalance(memberAccount.getBalance() - memberLostAmount);

        try {
            bid = bidRepository.save(bid);
            transactionRepository.save(transaction);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
        BidResponseDTO bidResponseDTO = bidMapper.toBidResponseDTO(bid);
        AuctionSessionResponseAccountDTO memberResponse = new AuctionSessionResponseAccountDTO();
        memberResponse.setId(memberAccount.getUser_id());
        memberResponse.setUsername(memberAccount.getUsername());
        memberResponse.setFullName(memberAccount.getFirstName() + " " + memberAccount.getLastName());
        bidResponseDTO.setMember(memberResponse);
        bidResponseDTO.setAuctionSessionId(auctionSession.getAuctionSessionId());
        return bidResponseDTO;
    }


    @Transactional
    public void buyNow(BuyNowRequestDTO buyNowRequestDTO) {//khi mức giá hiện tại của phiên đấu giá cao hơn giá buy
        // now -> disable buy now
        AuctionSession auctionSession = getAuctionSessionByID(buyNowRequestDTO.getAuctionSessionId());
        Account memberAccount = accountUtils.getCurrentAccount();
        if (auctionSession.getCurrentPrice() >= auctionSession.getBuyNowPrice()) {
            throw new BidException("Cannot use Buy Now when current session price is higher than buy now price");
        }
        if (memberAccount.getBalance() >= auctionSession.getBuyNowPrice()) {

            Account manager = accountRepository.findAccountByUsername("manager");
            //transaction 0 : member lost how much
            Transaction transaction0 = new Transaction();
            transaction0.setCreateAt(new Date());
            transaction0.setType(TransactionEnum.TRANSFER_FUNDS);
            transaction0.setFrom(memberAccount);
            transaction0.setTo(manager);
            transaction0.setAmount(auctionSession.getBuyNowPrice());
            transaction0.setDescription("Buy now (-) : " + auctionSession.getBuyNowPrice());
            transaction0.setAuctionSession(auctionSession);
            transaction0.setStatus(TransactionStatus.SUCCESS);
            memberAccount.setBalance(memberAccount.getBalance() - auctionSession.getBuyNowPrice());

            //transaction 1 : system get profit
            double serviceFeePercent = ServiceFeePercent.SERVICE_FEE_PERCENT;
            Transaction transaction = new Transaction();
            SystemProfit systemProfit = new SystemProfit();
            double profit = auctionSession.getBuyNowPrice() * serviceFeePercent;

            transaction.setCreateAt(new Date());
            transaction.setType(TransactionEnum.FEE_TRANSFER);
            transaction.setFrom(memberAccount);
            transaction.setTo(manager);
            transaction.setAmount(profit);
            transaction.setDescription("System take (+) " + profit + " as service fee");
            transaction.setStatus(TransactionStatus.SUCCESS);

            systemProfit.setBalance(increasedBalance(manager, profit));
            systemProfit.setDate(new Date());
            systemProfit.setDescription("System revenue increased (+) " + profit);
            transaction.setSystemProfit(systemProfit);
            transaction.setAuctionSession(auctionSession);
            systemProfit.setTransaction(transaction);

            //transaction 2 : transfer to koi breeder
            Transaction transaction2 = new Transaction();
            Account koiBreeder = auctionSession.getKoiFish().getAccount();
            double koiBreederAmount = auctionSession.getBuyNowPrice() - profit;
            transaction2.setCreateAt(new Date());
            transaction2.setType(TransactionEnum.TRANSFER_FUNDS);
            transaction2.setFrom(manager);
            transaction2.setTo(koiBreeder);
            transaction2.setAmount(koiBreederAmount);
            transaction2.setDescription("Get (+) " + koiBreederAmount + " from system ");
            transaction2.setAuctionSession(auctionSession);
            transaction2.setStatus(TransactionStatus.SUCCESS);
            koiBreeder.setBalance(increasedBalance(koiBreeder, koiBreederAmount));

            transaction0 = transactionRepository.save(transaction0);
            transaction = transactionRepository.save(transaction);
            transaction2 = transactionRepository.save(transaction2);
            accountRepository.save(memberAccount);

            Set<Transaction> transactionSet = new HashSet<>();
            transactionSet.add(transaction0);
            transactionSet.add(transaction);
            transactionSet.add(transaction2);

            auctionSession.setStatus(AuctionSessionStatus.COMPLETED);
            auctionSession.setWinner(accountUtils.getCurrentAccount());
            auctionSession.setNote("Auction completed by Buy Now on " + new Date());
            updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
            auctionSession.setUpdateAt(new Date());
            auctionSession.setTransactionSet(transactionSet);
            auctionSessionService.updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
            auctionSessionRepository.save(auctionSession);
            auctionSessionService.closeAuctionSessionWhenBuyNow(auctionSession);
        } else {
            throw new BidException("Your balance does not have enough money to buy");
        }
    }

    public double estimateTotalCost(BidRequestDTO requestDTO) {
        AuctionSession auctionSession = getAuctionSessionByID(requestDTO.getAuctionSessionId());
        Account member = accountUtils.getCurrentAccount();
        Set<Bid> bidSet = auctionSession.getBidSet();
        double totalCost;
        double previousMaxBidAmount = findMaxBidAmount(bidSet);
        if(previousMaxBidAmount == 0){
            previousMaxBidAmount = auctionSession.getCurrentPrice();
        }
        double currentBidAmount = previousMaxBidAmount + requestDTO.getBidAmount();
        Bid latestBidOfCurrentMember =
                bidRepository.getLatestBidAmountOfCurrentMemberOfAuctionSession(member.getUser_id(), auctionSession.getAuctionSessionId());
        if (latestBidOfCurrentMember != null) {
            totalCost = currentBidAmount - latestBidOfCurrentMember.getBidAmount();
        } else {
            totalCost = auctionSession.getCurrentPrice() + requestDTO.getBidAmount();
        }
        return totalCost;
    }

    public double findMaxBidAmount(Set<Bid> bidSet) {
        double maxBidAmount = 0;
        for (Bid bid : bidSet) {
            if (maxBidAmount < bid.getBidAmount()) {
                maxBidAmount = bid.getBidAmount();
            }
        }
        return maxBidAmount;
    }

    public void updateAuctionSessionCurrentPrice(double bidAmount, AuctionSession auctionSession) {
        double currentPrice = auctionSession.getCurrentPrice();
        auctionSession.setCurrentPrice(bidAmount);
        auctionSession.setUpdateAt(new Date());
        try {
            auctionSessionRepository.save(auctionSession);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public KoiFish getKoiFishByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);
        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        }
        return koiFish;
    }

    public void updateKoiStatus(Long id, AuctionSessionStatus status) {
        KoiFish target = getKoiFishByID(id);
        switch (status) {
            case UPCOMING, ONGOING: {
                target.setKoiStatus(KoiStatusEnum.SELLING);
                target.setUpdatedDate(new Date());
                break;
            }
            case COMPLETED, DRAWN: {
                target.setKoiStatus(KoiStatusEnum.SOLD);
                target.setUpdatedDate(new Date());
                break;
            }
            case CANCELLED, NO_WINNER: {
                target.setKoiStatus(KoiStatusEnum.AVAILABLE);
                target.setUpdatedDate(new Date());
                break;
            }
        }
        try {
            koiFishRepository.save(target);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    public AuctionSession getAuctionSessionByID(Long auction_session_id) {
        AuctionSession auctionSession = auctionSessionRepository.findAuctionSessionById(auction_session_id);
        if (auctionSession == null) {
            throw new EntityNotFoundException("Auction Session with id : " + auction_session_id + " not found");
        } else if (!auctionSession.getStatus().equals(AuctionSessionStatus.ONGOING)) {
            throw new EntityNotFoundException("Auction Session  with id : " + auction_session_id + " is not available" +
                    " to bid ");
        }
        return auctionSession;
    }

    public double increasedBalance(Account account, double amount) {
        double currentBalance = account.getBalance();
        double newBalance = currentBalance + amount;
        account.setBalance(newBalance);
        accountRepository.save(account);
        return newBalance;
    }


}