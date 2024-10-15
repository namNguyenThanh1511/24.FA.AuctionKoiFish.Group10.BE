package com.group10.koiauction.service;

import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.exception.BidException;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.BidRepository;
import com.group10.koiauction.model.request.BidRequestDTO;
import com.group10.koiauction.model.request.BuyNowRequestDTO;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private TransactionRepository transactionRepository;

    public Bid createBid(BidRequestDTO bidRequestDTO) {
        Account memberAccount = accountUtils.getCurrentAccount();
        AuctionSession auctionSession = getAuctionSessionByID(bidRequestDTO.getAuctionSessionId());
        if (memberAccount.getBalance() < auctionSession.getMinBalanceToJoin()) {
            throw new BidException("Your balance does not have enough money to join the auction." + "You currently " +
                    "have  " + memberAccount.getBalance() + " but required : " + auctionSession.getMinBalanceToJoin());
        }
        double maxBidAmount = findMaxBidAmount(auctionSession.getBidSet());
        if(maxBidAmount == 0){
            maxBidAmount = auctionSession.getCurrentPrice();
        }
        if (bidRequestDTO.getBidAmount() < maxBidAmount + auctionSession.getBidIncrement()) {
            throw new BidException("Your bid is lower than the required minimum increment.");
        }
        if (bidRequestDTO.getBidAmount() >= auctionSession.getBuyNowPrice()) {//khi đấu giá vượt quá Buy Now ->
            // chuyển sang buy now , ko tính là bid nữa
            throw new RuntimeException("You can buy now this fish");
        }
        memberAccount.setBalance(memberAccount.getBalance() - bidRequestDTO.getBidAmount());

        Bid bid = new Bid();
        bid.setBidAt(new Date());
        bid.setBidAmount(bidRequestDTO.getBidAmount());
        bid.setAuctionSession(getAuctionSessionByID(bidRequestDTO.getAuctionSessionId()));
        bid.setMember(memberAccount);
        updateAuctionSessionCurrentPrice(bid.getBidAmount(), auctionSession);

        Transaction transaction = new Transaction();
        transaction.setCreateAt(new Date());
        transaction.setAuctionSession(auctionSession);
        transaction.setFrom(memberAccount);
        transaction.setType(TransactionEnum.BID);
        transaction.setAmount(bidRequestDTO.getBidAmount());
        transaction.setDescription("Bidding : - "+bidRequestDTO.getBidAmount());

        transaction.setBid(bid);
        bid.setTransaction(transaction);
        try {
            bidRepository.save(bid);
            return bid;
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void buyNow(BuyNowRequestDTO buyNowRequestDTO) {//khi mức giá hiện tại của phiên đấu giá cao hơn giá buy
        // now -> disable buy now
        AuctionSession auctionSession = getAuctionSessionByID(buyNowRequestDTO.getAuctionSessionId());
        Account memberAccount = accountUtils.getCurrentAccount();
        if (auctionSession.getCurrentPrice() >= auctionSession.getBuyNowPrice()) {
            throw new BidException("Cannot use Buy Now when current session price is higher than buy now price");
        }
        if (memberAccount.getBalance() >= auctionSession.getBuyNowPrice()) {
            memberAccount.setBalance(memberAccount.getBalance() - auctionSession.getBuyNowPrice());
            auctionSession.setStatus(AuctionSessionStatus.COMPLETED);
            auctionSession.setWinner(accountUtils.getCurrentAccount());
            auctionSession.setNote("Auction completed by Buy Now on " + new Date());
            updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
            auctionSession.setUpdateAt(new Date());
            auctionSessionRepository.save(auctionSession);
        } else {
            throw new BidException("Your balance does not have enough money to buy");
        }
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
            case COMPLETED, DRAWN, WAITING_FOR_PAYMENT: {

                target.setKoiStatus(KoiStatusEnum.WAITING_FOR_PAYMENT);
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


}