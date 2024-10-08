package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.AuctionSessionType;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponseDTO;
import com.group10.koiauction.service.AuctionSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/auctionSession")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class AuctionSessionAPI {

    @Autowired
    private AuctionSessionService auctionSessionService;

    @PostMapping("")
    public ResponseEntity<AuctionSessionResponseDTO> createAuctionSession(@RequestBody AuctionSessionRequestDTO auctionSessionRequestDTO) {
        AuctionSessionResponseDTO auctionSessionResponseDTO = auctionSessionService.createAuctionSession(auctionSessionRequestDTO);
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

    @GetMapping()
    public ResponseEntity<List<AuctionSessionResponseDTO>> getAllAuctionSessions() {
        List<AuctionSessionResponseDTO> auctionSessionResponseDTOs = auctionSessionService.getAllAuctionSessions();
        return ResponseEntity.ok(auctionSessionResponseDTOs);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<AuctionSessionResponseDTO> updateAuctionSessionStatus(@PathVariable Long id, @RequestBody UpdateStatusAuctionSessionRequestDTO updateStatusAuctionSessionRequestDTO) {
        AuctionSessionResponseDTO auctionSessionResponseDTO = auctionSessionService.updateAuctionSessionStatus(id, updateStatusAuctionSessionRequestDTO);
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

}
