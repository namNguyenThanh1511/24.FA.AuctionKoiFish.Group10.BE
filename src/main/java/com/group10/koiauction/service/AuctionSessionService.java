package com.group10.koiauction.service;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponseDTO;
import com.group10.koiauction.repository.AuctionRequestRepository;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public AuctionSessionResponseDTO createAuctionSession(AuctionSessionRequestDTO auctionSessionRequestDTO) {
        AuctionSession auctionSession = auctionSessionMapper.toAuctionSession(auctionSessionRequestDTO);
        AuctionRequest auctionRequest = getAuctionRequestByID(auctionSessionRequestDTO.getAuction_request_id());
        auctionSession.setCurrentPrice(auctionSession.getStartingPrice());
        auctionSession.setKoiFish(auctionRequest.getKoiFish());//lay ca tu auction request
        auctionSession.setAuctionRequest(auctionRequest);
        auctionSession.setManager(accountUtils.getCurrentAccount());
        auctionSession.setStatus(AuctionSessionStatus.UPCOMING);
        updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(),auctionSession.getStatus());//update fish status based on AuctionSession status
        try {
            auctionSessionRepository.save(auctionSession);
        } catch (Exception e) {
            if (e.getMessage().contains("UK9849ywhqdd6e9e0q2gla07c7o")) {
                throw new DuplicatedEntity("auction request with id " + auctionSessionRequestDTO.getAuction_request_id() + " already been used in another auction session");
            }
            throw new RuntimeException(e.getMessage());
        }
        AuctionSessionResponseDTO auctionSessionResponseDTO = auctionSessionMapper.toAuctionSessionResponseDTO(auctionSession);
        auctionSessionResponseDTO.setManager_id(accountUtils.getCurrentAccount().getUser_id());
        auctionSessionResponseDTO.setKoi_id(auctionRequest.getKoiFish().getKoi_id());
        auctionSessionResponseDTO.setAuction_request_id(auctionRequest.getAuction_request_id());
        auctionSessionResponseDTO.setAuctionType(auctionSession.getAuctionType());
        return auctionSessionResponseDTO;
    }

    public List<AuctionSessionResponseDTO> getAllAuctionSessions() {
        List<AuctionSession> auctionSessions = auctionSessionRepository.findAll();
        List<AuctionSessionResponseDTO> auctionSessionResponseDTOs = new ArrayList<>();
        for (AuctionSession auctionSession : auctionSessions) {
            AuctionSessionResponseDTO auctionSessionResponseDTO = auctionSessionMapper.toAuctionSessionResponseDTO(auctionSession);
            auctionSessionResponseDTO.setManager_id(auctionSession.getManager().getUser_id());
            auctionSessionResponseDTO.setKoi_id(auctionSession.getKoiFish().getKoi_id());
            auctionSessionResponseDTO.setAuction_request_id(auctionSession.getAuctionRequest().getAuction_request_id());
            auctionSessionResponseDTOs.add(auctionSessionResponseDTO);
        }
        return auctionSessionResponseDTOs;
    }

    public AuctionSessionResponseDTO updateAuctionSessionStatus(Long auction_session_id, UpdateStatusAuctionSessionRequestDTO updateStatusAuctionSessionRequestDTO) {
        AuctionSession auctionSession = auctionSessionRepository.findById(auction_session_id).orElseThrow(() -> new EntityNotFoundException("Auction session with id " + auction_session_id + " not found"));
        auctionSession.setStatus(getAuctionSessionStatus(updateStatusAuctionSessionRequestDTO.getStatus()));
        auctionSession.setNote(updateStatusAuctionSessionRequestDTO.getNote());
        updateKoiStatus(auctionSession.getKoiFish().getKoi_id(),auctionSession.getStatus());
        try {
            auctionSessionRepository.save(auctionSession);

        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
        AuctionSessionResponseDTO auctionSessionResponseDTO = auctionSessionMapper.toAuctionSessionResponseDTO(auctionSession);
        auctionSessionResponseDTO.setKoi_id(auctionSession.getKoiFish().getKoi_id());
        auctionSessionResponseDTO.setAuction_request_id(auctionSession.getAuctionRequest().getAuction_request_id());
        auctionSessionResponseDTO.setManager_id(auctionSession.getManager().getUser_id());

        if (auctionSession.getWinner() == null) {
            auctionSessionResponseDTO.setWinner_id(null);
        } else {
            auctionSessionResponseDTO.setWinner_id(auctionSession.getWinner().getUser_id());
        }
        return auctionSessionResponseDTO;
    }

    public KoiFish getKoiFishByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);
        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        }
        return koiFish;
    }

    public AuctionRequest getAuctionRequestByID(Long auction_request_id) {
        AuctionRequest auctionRequest = auctionRequestRepository.findByAuctionRequestId(auction_request_id);
        if (auctionRequest == null) {
            throw new EntityNotFoundException("AuctionRequest with id : " + auction_request_id + " not found");
        } else if (!auctionRequest.getStatus().equals("APPROVED_BY_MANAGER")) {
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
            case UPCOMING, ONGOING:{
                target.setKoiStatus(KoiStatusEnum.SELLING);
                target.setUpdatedDate(new Date());
                break;
            }
            case COMPLETED, DRAWN, WAITING_FOR_PAYMENT:{

                target.setKoiStatus(KoiStatusEnum.WAITING_FOR_PAYMENT);
                target.setUpdatedDate(new Date());
                break;
            }case CANCELLED, NO_WINNER:{
                target.setKoiStatus(KoiStatusEnum.AVAILABLE);
                target.setUpdatedDate(new Date());
                break;
            }
        }
        try{
            koiFishRepository.save(target);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }


    }


}
