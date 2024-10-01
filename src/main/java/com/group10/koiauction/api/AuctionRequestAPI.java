package com.group10.koiauction.api;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.model.request.AuctionRequestDTO;
import com.group10.koiauction.model.request.AuctionRequestUpdateDTO;
import com.group10.koiauction.model.response.AuctionRequestResponse;
import com.group10.koiauction.service.AuctionRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctionRequest")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class AuctionRequestAPI {

    @Autowired
    AuctionRequestService auctionRequestService;

    @PostMapping()
    @PreAuthorize("hasAuthority('KOI_BREEDER')")
    public ResponseEntity<AuctionRequestResponse> createAuctionRequest(@RequestBody AuctionRequestDTO auctionRequestDTO) {
        AuctionRequestResponse newAuctionRequest = auctionRequestService.createAuctionRequest(auctionRequestDTO);
        return ResponseEntity.ok(newAuctionRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionRequestResponse> updateAuctionRequest(@PathVariable Long id,
                                                                       @Valid @RequestBody AuctionRequestUpdateDTO auctionRequestDTO) {
        AuctionRequestResponse updatedAuctionRequest = auctionRequestService.updateAuctionRequest(id,auctionRequestDTO);
        return ResponseEntity.ok(updatedAuctionRequest);
    }

    @GetMapping("")
    public ResponseEntity<List<AuctionRequestResponse>> getAllPendingAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests(
                "pending");
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuctionRequestResponse>> getAllAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests();
        return ResponseEntity.ok(auctionRequestResponseList);
    }


}
