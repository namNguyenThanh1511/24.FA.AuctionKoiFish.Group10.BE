package com.group10.koiauction.api;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.model.request.AuctionRequestDTO;
import com.group10.koiauction.model.request.AuctionRequestUpdateDTO;
import com.group10.koiauction.model.response.AuctionRequestResponse;
import com.group10.koiauction.service.AuctionRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctionRequest")
public class AuctionRequestAPI {

    @Autowired
    AuctionRequestService auctionRequestService;

    @PostMapping()
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

    @GetMapping("/all/pending")
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
