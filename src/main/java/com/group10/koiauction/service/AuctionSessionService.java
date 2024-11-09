package com.group10.koiauction.service;

import com.group10.koiauction.constant.MappingURL;
import com.group10.koiauction.constant.ServiceFeePercent;
import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.exception.BidException;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.AuctionRequestMapper;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.mapper.KoiMapper;
import com.group10.koiauction.model.MemberBidProjectionDTO;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.*;
import com.group10.koiauction.repository.*;
import com.group10.koiauction.service.job.ActivateAuctionSessionService;
import com.group10.koiauction.service.job.DeactivateAuctionSessionService;
import com.group10.koiauction.utilities.AccountUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static shaded_package.net.minidev.asm.ConvertDate.convertToDate;

@Service
public class AuctionSessionService {

    @Autowired
    AuctionSessionMapper auctionSessionMapper;

    @Autowired
    AuctionSessionRepository auctionSessionRepository;

    @Autowired
    AccountUtils accountUtils;

    @Autowired
    KoiFishRepository koiFishRepository;

    @Autowired
    AuctionRequestRepository auctionRequestRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KoiMapper koiMapper;

    @Autowired
    AuctionRequestMapper auctionRequestMapper;

    @Autowired
    KoiFishService koiFishService;

    @Autowired
    BidService bidService;

    @Autowired
    Scheduler scheduler;
    @Autowired
    private BidRepository bidRepository;

    @Autowired
    EmailService emailService;


    public AuctionSessionResponsePrimaryDataDTO createAuctionSession(AuctionSessionRequestDTO auctionSessionRequestDTO) {
        AuctionSession auctionSession = auctionSessionMapper.toAuctionSession(auctionSessionRequestDTO);
        AuctionRequest auctionRequest = getAuctionRequestByID(auctionSessionRequestDTO.getAuction_request_id());
        auctionSession.setCurrentPrice(auctionSession.getStartingPrice());
        auctionSession.setKoiFish(auctionRequest.getKoiFish());//lay ca tu auction request
        auctionSession.setAuctionRequest(auctionRequest);
        auctionSession.setStaff(getAccountById(auctionSessionRequestDTO.getStaff_id()));//phân công staff cho phiên đấu giá
        auctionSession.setManager(accountUtils.getCurrentAccount());
        auctionSession.setStatus(AuctionSessionStatus.UPCOMING);
        auctionSession.setCreateAt(new Date());
        auctionSession.setUpdateAt(auctionSession.getCreateAt());
        updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(), auctionSession.getStatus());//update fish status based on AuctionSession status
        try {
            auctionSession = auctionSessionRepository.save(auctionSession);
            scheduleActivationJob(auctionSession);

            // Create and send the email to the staff directly here
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setAccount(auctionSession.getKoiFish().getAccount());
            emailDetail.setSubject("New Auction Session Created");

            // Set the link to the auction session (you can adjust the URL path)
            String auctionLink = MappingURL.BASE_URL_LOCAL + "auctions/" + auctionSession.getAuctionSessionId();
            emailDetail.setLink(auctionLink);

            // Create a copy of the variables so they are final or effectively final in the lambda expression
            final EmailDetail finalEmailDetail = emailDetail;
            final AuctionSession finalAuctionSession = auctionSession;

            // Sending the email asynchronously
            Runnable runnable = () -> {
                emailService.sendAuctionSessionCreatedEmail(finalEmailDetail, finalAuctionSession); // Use the effectively final variables
            };
            new Thread(runnable).start();

        } catch (Exception e) {
            if (e.getMessage().contains("UK9849ywhqdd6e9e0q2gla07c7o")) {
                throw new DuplicatedEntity("auction request with id " + auctionSessionRequestDTO.getAuction_request_id() + " already been used in another auction session");
            }
            throw new RuntimeException(e.getMessage());
        }
        return getAuctionSessionResponsePrimaryDataDTO(auctionSession);
    }

    public void scheduleActivationJob(AuctionSession auctionSession) {
        try {
            // Tạo JobDetail cho ActivateAuctionSessionJob
            JobDetail activateJobDetail = JobBuilder.newJob(ActivateAuctionSessionService.class)
                    .withIdentity("activateAuctionSessionJob_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .usingJobData("auctionSessionId", auctionSession.getAuctionSessionId().toString())
                    .build();

            // Tạo Trigger cho ActivateAuctionSessionJob vào ngày `dateFrom`
            Trigger activateTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("activateTrigger_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .startAt(Date.from(auctionSession.getStartDate().atZone(ZoneId.systemDefault()).toInstant())) // Thời gian
                    // bắt
                    // đầu phiên
                    .build();

            // Lên lịch job kích hoạt phiên
            scheduler.scheduleJob(activateJobDetail, activateTrigger);

            // Tạo JobDetail cho DeactivateAuctionSessionJob
            JobDetail deactivateJobDetail = JobBuilder.newJob(DeactivateAuctionSessionService.class)
                    .withIdentity("deactivateAuctionSessionJob_" + auctionSession.getAuctionSessionId(),
                            "auctionSession")
                    .usingJobData("auctionSessionId", auctionSession.getAuctionSessionId().toString())
                    .build();

            // Tạo Trigger cho DeactivateAuctionSessionJob vào ngày `dateTo`
            Trigger deactivateTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("deactivateTrigger_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .startAt(Date.from(auctionSession.getEndDate().atZone(ZoneId.systemDefault()).toInstant())) // Thời gian
                    // kết
                    // thúc kỳ học
                    .build();

            // Lên lịch job hủy kích hoạt phien dau gia
            scheduler.scheduleJob(deactivateJobDetail, deactivateTrigger);

        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule jobs for semester activation and deactivation", e);
        }
    }

    public List<AuctionSessionResponsePrimaryDataDTO> getAllAuctionSessions() {
        List<AuctionSession> auctionSessions = auctionSessionRepository.findAll();
        List<AuctionSessionResponsePrimaryDataDTO> auctionSessionResponsePrimaryDataDTOS = new ArrayList<>();
        for (AuctionSession auctionSession : auctionSessions) {
            AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = getAuctionSessionResponsePrimaryDataDTO(auctionSession);
            auctionSessionResponsePrimaryDataDTOS.add(auctionSessionResponsePrimaryDataDTO);
        }
        return auctionSessionResponsePrimaryDataDTOS;
    }

    public AuctionSessionResponsePagination getAllAuctionSessionsPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.findAll(pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessions
                .stream()
                .map(this::getAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessions.getNumber(),
                auctionSessions.getSize(),
                auctionSessions.getTotalElements(),
                auctionSessions.getTotalPages()
        );
    }


    public AuctionSessionResponsePrimaryDataDTO updateAuctionSessionStatus(Long auction_session_id, UpdateStatusAuctionSessionRequestDTO updateStatusAuctionSessionRequestDTO) {
        AuctionSession auctionSession = auctionSessionRepository.findById(auction_session_id).orElseThrow(() -> new EntityNotFoundException("Auction session with id " + auction_session_id + " not found"));
        auctionSession.setStatus(getAuctionSessionStatus(updateStatusAuctionSessionRequestDTO.getStatus()));
        auctionSession.setNote(updateStatusAuctionSessionRequestDTO.getNote());
        updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
        auctionSession.setUpdateAt(new Date());
        try {
            auctionSessionRepository.save(auctionSession);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = getAuctionSessionResponsePrimaryDataDTO(auctionSession);
        return auctionSessionResponsePrimaryDataDTO;
    }

    @Transactional
    public AuctionSessionResponsePrimaryDataDTO closeAuctionSession(Long auction_session_id) {
        AuctionSession target = auctionSessionRepository.findById(auction_session_id).orElseThrow(() -> new EntityNotFoundException("Auction session with id " + auction_session_id + " not found"));
        Set<Bid> bidSet = target.getBidSet();
        if (bidSet.isEmpty()) {
            try {
                target.setUpdateAt(new Date());
                target.setNote("No participant");
                target.setStatus(AuctionSessionStatus.NO_WINNER);
                auctionSessionRepository.save(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            try {
                target.setWinner(getAuctionSessionWinner(target));
                target.setUpdateAt(new Date());
                target.setStatus(AuctionSessionStatus.COMPLETED);
                auctionSessionRepository.save(target);
                createTransactionsAfterAuctionSessionComplete(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
                returnMoneyAfterClosedAuctionSession(target);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        AuctionSessionResponsePrimaryDataDTO response = getAuctionSessionResponsePrimaryDataDTO(target);
        return response;
    }

    @Transactional
    public void closeAuctionSession(AuctionSession target) {
        if (target.getBidSet().isEmpty()) {
            try {
                target.setUpdateAt(new Date());
                target.setNote("No participant");
                target.setStatus(AuctionSessionStatus.NO_WINNER);
                auctionSessionRepository.save(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            try {
                target.setWinner(getAuctionSessionWinner(target));
                target.setUpdateAt(new Date());
                target.setStatus(AuctionSessionStatus.COMPLETED);
                auctionSessionRepository.save(target);
                createTransactionsAfterAuctionSessionComplete(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
                returnMoneyAfterClosedAuctionSession(target);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Transactional
    public void createTransactionsAfterAuctionSessionComplete(AuctionSession auctionSession) {
        Account manager = accountRepository.findAccountByUsername("manager");
        Account winner = auctionSession.getWinner();
        Transaction transaction = new Transaction();
        SystemProfit systemProfit = new SystemProfit();
        double serviceFeePercent = ServiceFeePercent.SERVICE_FEE_PERCENT;
        double profit = auctionSession.getCurrentPrice() * serviceFeePercent;

        transaction.setCreateAt(new Date());
        transaction.setType(TransactionEnum.TRANSFER_FUNDS);
        transaction.setFrom(winner);
        transaction.setTo(manager);
        transaction.setAmount(profit);
        transaction.setDescription("System take (+) " + profit + " as service fee");

        systemProfit.setBalance(bidService.increasedBalance(manager, profit));
        systemProfit.setDate(new Date());
        systemProfit.setDescription("System revenue increased (+) " + profit);
        transaction.setSystemProfit(systemProfit);
        transaction.setAuctionSession(auctionSession);
        transaction.setStatus(TransactionStatus.SUCCESS);
        systemProfit.setTransaction(transaction);

        Transaction transaction2 = new Transaction();
        Account koiBreeder = auctionSession.getKoiFish().getAccount();
        double koiBreederAmount = auctionSession.getCurrentPrice() - profit;
        transaction2.setCreateAt(new Date());
        transaction2.setType(TransactionEnum.TRANSFER_FUNDS);
        transaction2.setFrom(manager);
        transaction2.setTo(koiBreeder);
        transaction2.setAmount(koiBreederAmount);
        transaction2.setDescription("Get (+) " + koiBreederAmount + " from system ");
        transaction2.setAuctionSession(auctionSession);
        koiBreeder.setBalance(bidService.increasedBalance(koiBreeder, koiBreederAmount));
        transaction2.setStatus(TransactionStatus.SUCCESS);
        try {
            transactionRepository.save(transaction);
            transactionRepository.save(transaction2);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void returnMoneyAfterClosedAuctionSession(AuctionSession auctionSession) {
        List<MemberBidProjectionDTO> loserBids = bidRepository.findMaxBidForEachMemberInAuctionSessionExceptWinner(auctionSession.getAuctionSessionId(), auctionSession.getWinner().getUser_id());
        for (MemberBidProjectionDTO loserBid : loserBids) {
            createTransactionWhenReturnMoney(loserBid);
        }

    }

    @Transactional
    public void createTransactionWhenReturnMoney(MemberBidProjectionDTO loserBid) {
        Account loser = getAccountById(loserBid.getLoser_id());
        AuctionSession auctionSession =
                auctionSessionRepository.findById(loserBid.getAuction_session_id()).orElseThrow(() -> new EntityNotFoundException("No auction" +
                        " " +
                        "session found with id " + loserBid.getAuction_session_id()));
        Transaction transaction = new Transaction();
        transaction.setCreateAt(new Date());
        transaction.setType(TransactionEnum.TRANSFER_FUNDS);
        transaction.setFrom(null);
        transaction.setTo(loser);
        transaction.setAmount(loserBid.getBidAmount());
        transaction.setDescription("Return funds (+) : " + loserBid.getBidAmount());
        transaction.setAuctionSession(auctionSession);
        loser.setBalance(bidService.increasedBalance(loser, loserBid.getBidAmount()));
        transaction.setStatus(TransactionStatus.SUCCESS);
        try {
            transactionRepository.save(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Account getAuctionSessionWinner(AuctionSession auctionSession) {
        Set<Bid> bidSet = auctionSession.getBidSet();
        Bid maxBid = bidSet.stream().max(Comparator.comparing(Bid::getBidAmount)).orElseThrow(() -> new BidException("No bid found for this auction session"));
        return maxBid.getMember();
    }

    public KoiFish getKoiFishByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);
        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        }
        return koiFish;
    }

    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Staff with id " + id + " not found");
        }
        return account;
    }

    public AuctionRequest getAuctionRequestByID(Long auction_request_id) {
        AuctionRequest auctionRequest = auctionRequestRepository.findByAuctionRequestId(auction_request_id);
        if (auctionRequest == null) {
            throw new EntityNotFoundException("AuctionRequest with id : " + auction_request_id + " not found");
        } else if (!auctionRequest.getStatus().equals(AuctionRequestStatusEnum.APPROVED_BY_MANAGER)) {
            throw new EntityNotFoundException("AuctionRequest with id : " + auction_request_id + " is not approved by manager yet");
        }
        return auctionRequest;
    }

    public AuctionSessionStatus getAuctionSessionStatus(String status) {
        String statusX = status.toLowerCase().replaceAll("\\s", "");
        return switch (statusX) {
            case "upcoming" -> AuctionSessionStatus.UPCOMING;
            case "ongoing" -> AuctionSessionStatus.ONGOING;
            case "completed" -> AuctionSessionStatus.COMPLETED;
            case "cancelled" -> AuctionSessionStatus.CANCELLED;
            case "nowinner" -> AuctionSessionStatus.NO_WINNER;
            case "drawn" -> AuctionSessionStatus.DRAWN;
            case "waitingforpayment" -> AuctionSessionStatus.WAITING_FOR_PAYMENT;
            default -> throw new EntityNotFoundException("Invalid status");
        };
    }

    public void updateKoiStatus(Long id, AuctionSessionStatus status) {
        KoiFish target = getKoiFishByID(id);
        switch (status) {
            case UPCOMING: {
                target.setKoiStatus(KoiStatusEnum.PENDING_AUCTION);
                target.setUpdatedDate(new Date());
                break;
            }
            case ONGOING: {
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

    public AuctionSessionResponsePagination getAuctionSessionsByStaff(Long staffId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.findAllByStaffAccountId(staffId, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessions
                .stream()
                .map(this::getAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessions.getNumber(),
                auctionSessions.getSize(),
                auctionSessions.getTotalElements(),
                auctionSessions.getTotalPages()
        );
    }

    public AuctionSessionResponsePagination getAuctionSessionsByCurrentStaff(int page, int size) {
        Long currentAccountId = accountUtils.getCurrentAccount().getUser_id();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.findAllByStaffAccountId(currentAccountId, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessions
                .stream()
                .map(this::getAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessions.getNumber(),
                auctionSessions.getSize(),
                auctionSessions.getTotalElements(),
                auctionSessions.getTotalPages()
        );
    }

    public AuctionSessionResponsePagination getFilteredAuctionSessionsByCurrentStaff(
            AuctionSessionStatus auctionStatus,
            AuctionSessionType auctionType,
            int page,
            int size) {

        // Retrieve the current authenticated staff ID
        Long currentStaffId = accountUtils.getCurrentAccount().getUser_id();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.findByStaffIdAndFilters(
                currentStaffId, auctionStatus, auctionType, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessions
                .stream()
                .map(this::getAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessions.getNumber(),
                auctionSessions.getSize(),
                auctionSessions.getTotalElements(),
                auctionSessions.getTotalPages()
        );
    }

    public AuctionSessionResponsePagination searchAuctionSessions(
            AuctionSessionType auctionType,
            KoiSexEnum sex,
            String breederName,
            Set<String> varieties,
            Double minSizeCm,
            Double maxSizeCm,
            Double minWeightKg,
            Double maxWeightKg,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.searchAuctionSessions(
                auctionType, sex, breederName, varieties, minSizeCm, maxSizeCm, minWeightKg, maxWeightKg, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = new ArrayList<>();
        for (AuctionSession auctionSession : auctionSessions.getContent()) {
            AuctionSessionResponsePrimaryDataDTO dto = new AuctionSessionResponsePrimaryDataDTO();

            dto.setAuctionSessionId(auctionSession.getAuctionSessionId());
            dto.setTitle(auctionSession.getTitle());
            dto.setStartingPrice(auctionSession.getStartingPrice());
            dto.setCurrentPrice(auctionSession.getCurrentPrice());
            dto.setBuyNowPrice(auctionSession.getBuyNowPrice());
            dto.setBidIncrement(auctionSession.getBidIncrement());
            dto.setStartDate(Date.from(auctionSession.getStartDate().atZone(ZoneId.systemDefault()).toInstant()));
            dto.setEndDate(Date.from(auctionSession.getEndDate().atZone(ZoneId.systemDefault()).toInstant()));
            dto.setMinBalanceToJoin(auctionSession.getMinBalanceToJoin());
            dto.setAuctionType(auctionSession.getAuctionType());
            dto.setAuctionStatus(auctionSession.getStatus());

            if (auctionSession.getKoiFish() != null) {
                AuctionSessionResponseKoiDTO koiDTO = new AuctionSessionResponseKoiDTO();
                koiDTO.setId(auctionSession.getKoiFish().getKoi_id());
                koiDTO.setName(auctionSession.getKoiFish().getName());
                koiDTO.setSex(auctionSession.getKoiFish().getSex());
                koiDTO.setSizeCm(auctionSession.getKoiFish().getSizeCm()); // Assuming KoiFish has getSizeCm()
                koiDTO.setWeightKg(auctionSession.getKoiFish().getWeightKg()); // Assuming KoiFish has getWeightKg()
                koiDTO.setBornIn(auctionSession.getKoiFish().getBornIn());
                koiDTO.setImage_url(auctionSession.getKoiFish().getImage_url());
                koiDTO.setDescription(auctionSession.getKoiFish().getDescription());
                koiDTO.setEstimatedValue(auctionSession.getKoiFish().getEstimatedValue());
                koiDTO.setKoiStatus(auctionSession.getKoiFish().getKoiStatus());
                koiDTO.setVideo_url(auctionSession.getKoiFish().getVideo_url());

                // Map breeder details
                BreederResponseDTO breederDTO = new BreederResponseDTO();
                breederDTO.setId(auctionSession.getKoiFish().getAccount().getUser_id());
                breederDTO.setUsername(auctionSession.getKoiFish().getAccount().getUsername());
                koiDTO.setBreeder(breederDTO);

                // Map varieties
                Set<Variety> varietiesSet = auctionSession.getKoiFish().getVarieties();
                koiDTO.setVarieties(varietiesSet);

                koiDTO.setVarieties(koiDTO.getVarieties());
                dto.setKoi(koiDTO);
            }

            if (auctionSession.getAuctionRequest() != null) {
                AuctionSessionResponseAuctionRequestDTO requestDTO = new AuctionSessionResponseAuctionRequestDTO();
                requestDTO.setAuction_request_id(auctionSession.getAuctionRequest().getAuction_request_id());
                requestDTO.setTitle(auctionSession.getAuctionRequest().getTitle());
                requestDTO.setCreatedDate(auctionSession.getAuctionRequest().getCreatedDate());
                requestDTO.setDescription(auctionSession.getAuctionRequest().getDescription());
                requestDTO.setResponse_note(auctionSession.getAuctionRequest().getResponse_note());
                requestDTO.setStatus(auctionSession.getAuctionRequest().getStatus());
                dto.setAuctionRequest(requestDTO);
            }

            if (auctionSession.getWinner() != null) {
                AuctionSessionResponseAccountDTO winnerDTO = new AuctionSessionResponseAccountDTO();
                winnerDTO.setId(auctionSession.getWinner().getUser_id());
                winnerDTO.setUsername(auctionSession.getWinner().getUsername());
                winnerDTO.setFullName(auctionSession.getWinner().getFirstName() + " " + auctionSession.getWinner().getLastName());
                dto.setWinner(winnerDTO);
            }

            if (auctionSession.getStaff() != null) {
                AuctionSessionResponseAccountDTO staffDTO = new AuctionSessionResponseAccountDTO();
                staffDTO.setId(auctionSession.getStaff().getUser_id());
                staffDTO.setUsername(auctionSession.getStaff().getUsername());
                staffDTO.setFullName(auctionSession.getStaff().getFirstName() + " " + auctionSession.getStaff().getLastName());
                dto.setStaff(staffDTO);
            }

            if (auctionSession.getManager() != null) {
                AuctionSessionResponseAccountDTO managerDTO = new AuctionSessionResponseAccountDTO();
                managerDTO.setId(auctionSession.getManager().getUser_id());
                managerDTO.setUsername(auctionSession.getManager().getUsername());
                managerDTO.setFullName(auctionSession.getManager().getFirstName() + " " + auctionSession.getManager().getLastName());
                dto.setManager(managerDTO);
            }

            // Map bids
            List<BidResponseDTO> bidDTOs = auctionSession.getBidSet().stream().map(bid -> {
                BidResponseDTO bidDTO = new BidResponseDTO();
                bidDTO.setId(bid.getId());
                bidDTO.setBidAmount(bid.getBidAmount());
                bidDTO.setBidAt(bid.getBidAt());
                bidDTO.setAutoBid(bid.isAutoBid());

                // Map member details for the bid
                AuctionSessionResponseAccountDTO memberDTO = new AuctionSessionResponseAccountDTO();
                memberDTO.setId(bid.getMember().getUser_id());
                memberDTO.setUsername(bid.getMember().getUsername());
                memberDTO.setFullName(bid.getMember().getFirstName() + " " + bid.getMember().getLastName());
                bidDTO.setMember(memberDTO);

                bidDTO.setAuctionSessionId(auctionSession.getAuctionSessionId());

                return bidDTO;
            }).collect(Collectors.toList());

            dto.setBids(bidDTOs);

            responseList.add(dto);
        }

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessions.getNumber(),
                auctionSessions.getSize(),
                auctionSessions.getTotalElements(),
                auctionSessions.getTotalPages()
        );
    }

    public AuctionSessionResponsePagination getAuctionSessionsByCurrentUser(int page, int size) {
        Long currentUserId = accountUtils.getCurrentAccount().getUser_id();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessionsPage = auctionSessionRepository.findLatestBidAuctionSessionsByUserId(currentUserId, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessionsPage.stream()
                .map(this::convertToAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        return new AuctionSessionResponsePagination(
                responseList,
                auctionSessionsPage.getNumber(),
                auctionSessionsPage.getSize(),
                auctionSessionsPage.getTotalElements(),
                auctionSessionsPage.getTotalPages()
        );
    }
    private AuctionSessionResponsePrimaryDataDTO convertToAuctionSessionResponsePrimaryDataDTO(AuctionSession auctionSession) {
        AuctionSessionResponsePrimaryDataDTO responseDTO = new AuctionSessionResponsePrimaryDataDTO();
        responseDTO.setAuctionSessionId(auctionSession.getAuctionSessionId());
        responseDTO.setTitle(auctionSession.getTitle());
        responseDTO.setStartingPrice(auctionSession.getStartingPrice());
        responseDTO.setCurrentPrice(auctionSession.getCurrentPrice());
        responseDTO.setBuyNowPrice(auctionSession.getBuyNowPrice());
        responseDTO.setBidIncrement(auctionSession.getBidIncrement());

        // Use the conversion method here
        responseDTO.setStartDate(convertLocalDateTimeToDate(auctionSession.getStartDate()));
        responseDTO.setEndDate(convertLocalDateTimeToDate(auctionSession.getEndDate()));

        responseDTO.setMinBalanceToJoin(auctionSession.getMinBalanceToJoin());
        responseDTO.setAuctionStatus(auctionSession.getStatus());

        return responseDTO;
    }

    private Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null; // or throw an exception if null is not acceptable
    }

    private AuctionSessionResponseAccountDTO getAccountDTO(Account account) {
        if (account == null) {
            return new AuctionSessionResponseAccountDTO(); // Return a default instance
        }
        AuctionSessionResponseAccountDTO accountDTO = new AuctionSessionResponseAccountDTO();
        accountDTO.setId(account.getUser_id());
        accountDTO.setUsername(account.getUsername());
        accountDTO.setFullName(account.getFirstName() + " " + account.getLastName());
        return accountDTO;
    }

    public AuctionSessionResponsePrimaryDataDTO getAuctionSessionResponsePrimaryDataDTO(Long id) {
        AuctionSession auctionSession = auctionSessionRepository.findAuctionSessionById(id);
        return getAuctionSessionResponsePrimaryDataDTO(auctionSession);
    }

    public AuctionSessionResponsePrimaryDataDTO getAuctionSessionResponsePrimaryDataDTO(AuctionSession auctionSession) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionMapper.toAuctionSessionResponsePrimaryDataDto(auctionSession);
        AuctionSessionResponseAccountDTO staff = new AuctionSessionResponseAccountDTO();
        AuctionSessionResponseAccountDTO manager = new AuctionSessionResponseAccountDTO();
        AuctionSessionResponseAccountDTO winner = new AuctionSessionResponseAccountDTO();
        BreederResponseDTO breeder = new BreederResponseDTO();
        List<BidResponseDTO> bidsResponseList = new ArrayList<>();
        AuctionSessionResponseKoiDTO koi = koiMapper.toAuctionSessionResponseKoiDTO(auctionSession.getKoiFish());
        AuctionSessionResponseAuctionRequestDTO auctionRequest =
                auctionRequestMapper.toAuctionSessionResponseAuctionRequestDTO(auctionSession.getAuctionRequest());
        //----------------------------------------------------------------
        breeder.setId(auctionSession.getKoiFish().getAccount().getUser_id());
        breeder.setUsername(auctionSession.getKoiFish().getAccount().getUsername());
        koi.setId(auctionSession.getKoiFish().getKoi_id());
        koi.setBreeder(breeder);
        koi.setVideo_url(auctionSession.getKoiFish().getVideo_url());
        staff.setId(auctionSession.getStaff().getUser_id());
        staff.setUsername(auctionSession.getStaff().getUsername());
        staff.setFullName(auctionSession.getStaff().getFirstName() + " " + auctionSession.getStaff().getLastName());
        manager.setId(auctionSession.getManager().getUser_id());
        manager.setUsername(auctionSession.getManager().getUsername());
        manager.setFullName(auctionSession.getManager().getFirstName() + " " + auctionSession.getManager().getLastName());
        //----------------------------------------------------------------
        auctionSessionResponsePrimaryDataDTO.setStaff(staff);
        auctionSessionResponsePrimaryDataDTO.setManager(manager);
        if (auctionSession.getWinner() == null) {
            auctionSessionResponsePrimaryDataDTO.setWinner(null);
        } else {
            winner.setId(auctionSession.getWinner().getUser_id());
            winner.setUsername(auctionSession.getWinner().getUsername());
            winner.setFullName(auctionSession.getWinner().getFirstName() + " " + auctionSession.getWinner().getLastName());
            auctionSessionResponsePrimaryDataDTO.setWinner(winner);
        }
        auctionSessionResponsePrimaryDataDTO.setKoi(koi);
        auctionSessionResponsePrimaryDataDTO.setAuctionRequest(auctionRequest);
        auctionSessionResponsePrimaryDataDTO.setAuctionType(auctionSession.getAuctionType());
        auctionSessionResponsePrimaryDataDTO.setAuctionStatus(auctionSession.getStatus());
        if (auctionSession.getBidSet() == null) {
            auctionSessionResponsePrimaryDataDTO.setBids(null);
        } else {
            for (Bid bid : auctionSession.getBidSet()) {
                BidResponseDTO bidResponseDTO = getBidResponseDTO(auctionSession, bid);
                bidsResponseList.add(bidResponseDTO);
            }
//        Collections.sort(bidsResponseList, new Comparator<BidResponseDTO>()
//            @Override
//            public int compare(BidResponseDTO o1, BidResponseDTO o2) {
//                return o2.getBidAt().compareTo(o1.getBidAt());//desc latest to oldest
//            }
//        });
            //java 8 :
            bidsResponseList.sort(Comparator.comparing(BidResponseDTO::getBidAt).reversed());
            auctionSessionResponsePrimaryDataDTO.setBids(bidsResponseList);
        }
        return auctionSessionResponsePrimaryDataDTO;
    }

    private static BidResponseDTO getBidResponseDTO(AuctionSession auctionSession, Bid bid) {
        BidResponseDTO bidResponseDTO = new BidResponseDTO();
        bidResponseDTO.setId(bid.getId());
        bidResponseDTO.setAuctionSessionId(auctionSession.getAuctionSessionId());
        bidResponseDTO.setBidAt(bid.getBidAt());
        bidResponseDTO.setBidAmount(bid.getBidAmount());
        Account member = bid.getMember();
        AuctionSessionResponseAccountDTO memberResponse = new AuctionSessionResponseAccountDTO();
        memberResponse.setId(bid.getMember().getUser_id());
        memberResponse.setUsername(member.getUsername());
        memberResponse.setFullName(member.getFirstName() + " " + member.getLastName());
        bidResponseDTO.setMember(memberResponse);
        bidResponseDTO.setAutoBid(bid.isAutoBid());
        return bidResponseDTO;
    }

    public AuctionSessionResponsePrimaryDataDTO placeBid(Long auctionId, Long userId, boolean isBuyNow, double bidAmount) {
        // Find the auction session and user
        AuctionSession auctionSession = auctionSessionRepository.findByIdAndStatus(auctionId, AuctionSessionStatus.ONGOING)
                .orElseThrow(() -> new RuntimeException("Auction not found or not active"));

        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the user has already placed a bid in this auction session
        boolean hasBid = auctionSession.getBidSet().stream()
                .anyMatch(bid -> bid.getMember().getUser_id() == userId);

        if (hasBid) {
            throw new RuntimeException("User has already placed a bid");
        }

        // If the user chose Buy Now
        if (isBuyNow) {
            auctionSession.setWinner(user);
            auctionSession.setStatus(AuctionSessionStatus.COMPLETED);
            auctionSessionRepository.save(auctionSession);
            return convertToAuctionSessionResponsePrimaryDataDTO(auctionSession);
        }

        // Place bid with the specified bid amount
        Bid bid = new Bid();
        bid.setAuctionSession(auctionSession);
        bid.setMember(user);
        bid.setBidAmount(bidAmount);
        bid.setBidAt(new Date());
        auctionSession.getBidSet().add(bid);
        auctionSessionRepository.save(auctionSession);

        return convertToAuctionSessionResponsePrimaryDataDTO(auctionSession);
    }

    public AuctionSessionResponsePrimaryDataDTO finalizeAuctionSession(Long auctionId) {
        AuctionSession auctionSession = auctionSessionRepository.findByIdAndStatus(auctionId, AuctionSessionStatus.ONGOING)
                .orElseThrow(() -> new RuntimeException("Auction not found or not active"));

        if (auctionSession.getBidSet().isEmpty()) {
            // No participants in the auction session
            auctionSession.setUpdateAt(new Date());
            auctionSession.setNote("No participant");
            auctionSession.setStatus(AuctionSessionStatus.NO_WINNER);
            auctionSessionRepository.save(auctionSession);
            updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
        } else {
            // Select a random winner from the participants
            List<Bid> eligibleBids = new ArrayList<>(auctionSession.getBidSet());
            Account winner = eligibleBids.get(new Random().nextInt(eligibleBids.size())).getMember();

            auctionSession.setWinner(winner);
            auctionSession.setStatus(AuctionSessionStatus.COMPLETED);
            auctionSessionRepository.save(auctionSession);

            // Additional actions after the auction is complete
            createTransactionsAfterAuctionSessionComplete(auctionSession);
            updateKoiStatus(auctionSession.getKoiFish().getKoi_id(), auctionSession.getStatus());
            returnMoneyAfterClosedAuctionSession(auctionSession);
        }

        return convertToAuctionSessionResponsePrimaryDataDTO(auctionSession);
    }
    public AuctionSessionResponsePrimaryDataDTO processAuctionSessionById(Long auctionSessionId) {
        AuctionSession auctionSession = auctionSessionRepository.findById(auctionSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction session not found"));
        return processAuctionSession(auctionSession);
    }

    public AuctionSessionResponsePrimaryDataDTO processAuctionSession(AuctionSession auctionSession) {
        if (auctionSession.getAuctionType() == AuctionSessionType.ASCENDING) {
            return closeAuctionSession(auctionSession.getAuctionSessionId());
        } else if (auctionSession.getAuctionType() == AuctionSessionType.FIXED_PRICE) {
            return finalizeAuctionSession(auctionSession.getAuctionSessionId()); // Return the DTO from finalizeAuctionSession
        } else {
            throw new UnsupportedOperationException("Unsupported auction type: " + auctionSession.getAuctionType());
        }
    }
}
