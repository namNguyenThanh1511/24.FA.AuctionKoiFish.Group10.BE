package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.AuctionRequestMapper;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.mapper.KoiMapper;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.*;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.AuctionRequestRepository;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    KoiMapper koiMapper;

    @Autowired
    AuctionRequestMapper auctionRequestMapper;

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
            auctionSession=  auctionSessionRepository.save(auctionSession);
            scheduleActivationJob(auctionSession);

        } catch (Exception e) {
            if (e.getMessage().contains("UK9849ywhqdd6e9e0q2gla07c7o")) {
                throw new DuplicatedEntity("auction request with id " + auctionSessionRequestDTO.getAuction_request_id() + " already been used in another auction session");
            }
            throw new RuntimeException(e.getMessage());
        }
        return getAuctionSessionResponsePrimaryDataDTO(auctionSession);
    }
    private void scheduleActivationJob(AuctionSession auctionSession) {
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
                    // đầu kỳ học
                    .build();

            // Lên lịch job kích hoạt kỳ học
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
