package com.group10.koiauction.api;

import com.group10.koiauction.entity.Bid;
import com.group10.koiauction.model.request.BidRequestDTO;
import com.group10.koiauction.model.request.BuyNowRequestDTO;
import com.group10.koiauction.model.response.BidResponseDTO;
import com.group10.koiauction.service.BidService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bid")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class BidAPI {

    @Autowired
    private BidService bidService;

    @PostMapping("")
    public ResponseEntity<BidResponseDTO> createBid(@RequestBody BidRequestDTO bidRequestDTO) {
        BidResponseDTO bid = bidService.createBid(bidRequestDTO);
        return ResponseEntity.ok(bid);
    }

    @PostMapping("/buyNow")
    public ResponseEntity buyNow(@RequestBody BuyNowRequestDTO buyNowRequestDTO) {
        bidService.buyNow1(buyNowRequestDTO);
        return ResponseEntity.ok("Buy successful");
    }

    @PostMapping("/estimate-total-lost")
    public ResponseEntity estimateTotalLost(@RequestBody BidRequestDTO bidRequestDTO) {
        double totalLost = bidService.estimateTotalCost(bidRequestDTO);
        return ResponseEntity.ok("Total lost: " + totalLost);
    }


}
