package com.group10.koiauction.api;

import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponsePagination;
import com.group10.koiauction.model.response.AuctionSessionResponsePrimaryDataDTO;
import com.group10.koiauction.service.AuctionSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/auctionSession")
@CrossOrigin("*")
@SecurityRequirement(name = "api")// để sử dụng token tren swagger
public class AuctionSessionAPI {

    @Autowired
    private AuctionSessionService auctionSessionService;

    @GetMapping("/{id}")
    public ResponseEntity getAuctionSession(@PathVariable Long id) {
        AuctionSessionResponsePrimaryDataDTO response = auctionSessionService.getAuctionSessionResponsePrimaryDataDTO(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> createAuctionSession(@RequestBody AuctionSessionRequestDTO auctionSessionRequestDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponseDTO = auctionSessionService.createAuctionSession(auctionSessionRequestDTO);
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

    @GetMapping()
    public ResponseEntity<List<AuctionSessionResponsePrimaryDataDTO>> getAllAuctionSessions() {
        List<AuctionSessionResponsePrimaryDataDTO> auctionSessionResponseDTOs = auctionSessionService.getAllAuctionSessions();
        return ResponseEntity.ok(auctionSessionResponseDTOs);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> updateAuctionSessionStatus(@PathVariable Long id, @RequestBody UpdateStatusAuctionSessionRequestDTO updateStatusAuctionSessionRequestDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponseDTO = auctionSessionService.updateAuctionSessionStatus(id, updateStatusAuctionSessionRequestDTO);
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<AuctionSessionResponsePagination> getAuctionSessionsByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getAuctionSessionsByStaff(staffId, page, size);
        return ResponseEntity.ok(response);
    }

}
