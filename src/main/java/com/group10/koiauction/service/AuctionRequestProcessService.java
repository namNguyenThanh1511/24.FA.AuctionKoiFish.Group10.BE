package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionRequestProcess;
import com.group10.koiauction.mapper.AuctionRequestMapper;
import com.group10.koiauction.model.response.AuctionRequestProcessResponseDTO;
import com.group10.koiauction.model.response.AuctionRequestResponse;
import com.group10.koiauction.model.response.AuctionSessionResponseAccountDTO;
import com.group10.koiauction.model.response.BreederResponseDTO;
import com.group10.koiauction.repository.AuctionRequestProcessRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuctionRequestProcessService {

    @Autowired
    AuctionRequestProcessRepository auctionRequestProcessRepository;

    @Autowired
    AuctionRequestMapper auctionRequestMapper;

    public List<AuctionRequestProcessResponseDTO> getAllAuctionRequestProcess() {
        List<AuctionRequestProcess> auctionRequestProcessList = auctionRequestProcessRepository.findAll();
        List<AuctionRequestProcessResponseDTO> auctionRequestProcessResponseDTOList = new ArrayList<>();
        for (AuctionRequestProcess auctionRequestProcess : auctionRequestProcessList) {
            AuctionRequestProcessResponseDTO auctionRequestProcessResponseDTO = getAuctionRequestProcessResponseDTO(auctionRequestProcess);
            auctionRequestProcessResponseDTOList.add(auctionRequestProcessResponseDTO);
        }

        return auctionRequestProcessResponseDTOList;
    }

    public AuctionRequestProcessResponseDTO getAuctionRequestProcessResponseDTO(AuctionRequestProcess auctionRequestProcess) {
        AuctionRequest auctionRequest = auctionRequestProcess.getAuctionRequest();
        Account breeder = auctionRequest.getAccount();
        Account staff = auctionRequestProcess.getStaff();
        Account manager = auctionRequestProcess.getManager();



        BreederResponseDTO breederResponseDTO = new BreederResponseDTO();
        breederResponseDTO.setId(breeder.getUser_id());
        breederResponseDTO.setUsername(breeder.getUsername());

        AuctionSessionResponseAccountDTO staffResponse = new AuctionSessionResponseAccountDTO();
        if(staff != null) {

            staffResponse.setId(staff.getUser_id());
            staffResponse.setUsername(staff.getUsername());
            staffResponse.setFullName(staff.getFirstName() + " " + staff.getLastName());
        }

        AuctionSessionResponseAccountDTO managerResponse = new AuctionSessionResponseAccountDTO();
        if(manager != null) {

            managerResponse.setId(manager.getUser_id());
            managerResponse.setUsername(manager.getUsername());
            managerResponse.setFullName(manager.getFirstName() + " " + manager.getLastName());
        }

        AuctionRequestProcessResponseDTO responseDTO = new AuctionRequestProcessResponseDTO();
        responseDTO.setId(auctionRequestProcess.getId());
        responseDTO.setDate(auctionRequestProcess.getDate());
        responseDTO.setStatus(auctionRequestProcess.getStatus());


        AuctionRequestResponse auctionRequestResponse = auctionRequestMapper.toAuctionRequestResponse(auctionRequest);
        auctionRequestResponse.setBreeder(breederResponseDTO);
        auctionRequestResponse.setKoi_id(auctionRequest.getKoiFish().getKoi_id());

        responseDTO.setAuctionRequest(auctionRequestResponse);
        responseDTO.setStaff(staffResponse);
        responseDTO.setManager(managerResponse);
        return responseDTO;

    }

}
