package com.group10.koiauction.service;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponseDTO;
import com.group10.koiauction.repository.AuctionRequestRepository;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        }
        return auctionRequest;
    }

}
