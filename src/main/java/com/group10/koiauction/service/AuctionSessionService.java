package com.group10.koiauction.service;

import com.group10.koiauction.constant.ServiceFeePercent;
import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.exception.BidException;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.AuctionRequestMapper;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.mapper.KoiMapper;
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

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

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
            // Tạo JobDetail cho ActivateSemesterJob
            JobDetail activateJobDetail = JobBuilder.newJob(ActivateAuctionSessionService.class)
                    .withIdentity("activateAuctionSessionJob_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .usingJobData("auctionSessionId", auctionSession.getAuctionSessionId().toString())
                    .build();

            // Tạo Trigger cho ActivateSemesterJob vào ngày `dateFrom`
            Trigger activateTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("activateTrigger_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .startAt(Date.from(auctionSession.getStartDate().atZone(ZoneId.systemDefault()).toInstant())) // Thời gian
                    // bắt
                    // đầu phiên
                    .build();

            // Lên lịch job kích hoạt phiên
            scheduler.scheduleJob(activateJobDetail, activateTrigger);

            // Tạo JobDetail cho DeactivateSemesterJob
            JobDetail deactivateJobDetail = JobBuilder.newJob(DeactivateAuctionSessionService.class)
                    .withIdentity("deactivateAuctionSessionJob_" + auctionSession.getAuctionSessionId(),
                            "auctionSession")
                    .usingJobData("auctionSessionId", auctionSession.getAuctionSessionId().toString())
                    .build();

            // Tạo Trigger cho DeactivateSemesterJob vào ngày `dateTo`
            Trigger deactivateTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("deactivateTrigger_" + auctionSession.getAuctionSessionId(), "auctionSessions")
                    .startAt(Date.from(auctionSession.getEndDate().atZone(ZoneId.systemDefault()).toInstant())) // Thời gian
                    // kết
                    // thúc kỳ học
                    .build();

            // Lên lịch job hủy kích hoạt kỳ học
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
        Set<Bid> bidSet =  target.getBidSet();
        if(bidSet.isEmpty()){
            try {
                target.setUpdateAt(new Date());
                target.setNote("No participant");
                target.setStatus(AuctionSessionStatus.NO_WINNER);
                auctionSessionRepository.save(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        }else{
            try {
                target.setWinner(getAuctionSessionWinner(target));
                target.setUpdateAt(new Date());
                target.setStatus(AuctionSessionStatus.COMPLETED);
                auctionSessionRepository.save(target);
                createTransactionsAfterAuctionSessionComplete(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        AuctionSessionResponsePrimaryDataDTO response = getAuctionSessionResponsePrimaryDataDTO(target);
        return response;
    }

    @Transactional
    public void closeAuctionSession(AuctionSession target) {
        if(target.getBidSet().isEmpty()){
            try {
                target.setUpdateAt(new Date());
                target.setNote("No participant");
                target.setStatus(AuctionSessionStatus.NO_WINNER);
                auctionSessionRepository.save(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
            } catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage());
            }
        }else{
            try {
                target.setWinner(getAuctionSessionWinner(target));
                target.setUpdateAt(new Date());
                target.setStatus(AuctionSessionStatus.COMPLETED);
                auctionSessionRepository.save(target);
                createTransactionsAfterAuctionSessionComplete(target);
                updateKoiStatus(target.getKoiFish().getKoi_id(), target.getStatus());
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
        try {
            transactionRepository.save(transaction);
            transactionRepository.save(transaction2);
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

    public AuctionSessionResponsePagination searchAuctionSessions(
            AuctionSessionType auctionType,
            KoiSexEnum sex,
            String breederName,
            Set<String> varieties,  // Change to Set<String>
            Double minSizeCm,
            Double maxSizeCm,
            Double minWeightKg,
            Double maxWeightKg,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessions = auctionSessionRepository.searchAuctionSessions(
                auctionType, sex, breederName, varieties, minSizeCm, maxSizeCm, minWeightKg, maxWeightKg, pageable);

        List<AuctionSessionResponsePrimaryDataDTO> responseList = auctionSessions.getContent()
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

    public AuctionSessionResponsePagination getAuctionSessionsByCurrentUser(int page, int size) {
        Long currentUserId = accountUtils.getCurrentAccount().getUser_id(); // Lấy ID của user hiện tại

        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionSession> auctionSessionsPage = auctionSessionRepository.findAuctionSessionsByUserId(currentUserId, pageable);

        // Chuyển đổi từ entity sang DTO
        List<AuctionSessionResponsePrimaryDataDTO> auctionSessionResponses = auctionSessionsPage.stream()
                .map(this::convertToAuctionSessionResponsePrimaryDataDTO)
                .collect(Collectors.toList());

        // Trả về đối tượng phân trang
        return new AuctionSessionResponsePagination(
                auctionSessionResponses,
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
        responseDTO.getStartDate();
        responseDTO.getEndDate();
        responseDTO.setAuctionStatus(auctionSession.getStatus());

        return responseDTO;
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
        return auctionSessionResponsePrimaryDataDTO;

    }


}
