package com.group10.koiauction.api;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.model.request.AuctionRequestDTO;
import com.group10.koiauction.model.request.AuctionRequestUpdateDTO;
import com.group10.koiauction.model.request.ResponseAuctionRequestDTO;
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
@SecurityRequirement(name = "api")// để sử dụng token tren swagger
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
        AuctionRequestResponse updatedAuctionRequest = auctionRequestService.updateAuctionRequest(id, auctionRequestDTO);
        return ResponseEntity.ok(updatedAuctionRequest);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AuctionRequestResponse>> getAllPendingAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests(
                "pending");
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @GetMapping("")
    public ResponseEntity<List<AuctionRequestResponse>> getAllAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests("");
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @GetMapping("/koiBreeder")
    public ResponseEntity<List<AuctionRequestResponse>> getAllAuctionRequestsForCurrentKoiBreeder() {
        List<AuctionRequestResponse> auctionRequestResponseList =
                auctionRequestService.getAllAuctionRequestsOfCurrentKoiBreeder();
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<AuctionRequestResponse> approveAuctionRequest(@PathVariable Long id,
                                                                        @RequestBody ResponseAuctionRequestDTO responseAuctionRequestDTO) {
        AuctionRequestResponse auctionRequestResponse =
                auctionRequestService.approveAuctionRequest(id, responseAuctionRequestDTO);
        return ResponseEntity.ok(auctionRequestResponse);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<AuctionRequestResponse> rejectAuctionRequest(@PathVariable Long id,
                                                                       @RequestBody ResponseAuctionRequestDTO responseAuctionRequestDTO) {
        AuctionRequestResponse auctionRequestResponse =
                auctionRequestService.rejectAuctionRequest(id, responseAuctionRequestDTO);
        return ResponseEntity.ok(auctionRequestResponse);
    }

    @PutMapping("/revertApprove/{id}")
    public ResponseEntity<String> revertApprovalAuctionRequest(@PathVariable Long id){
        auctionRequestService.revertApproveAuctionRequest(id);
        return ResponseEntity.ok("Revert request successful");
    }

}
