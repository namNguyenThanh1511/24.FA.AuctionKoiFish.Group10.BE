package com.group10.koiauction.api;

import com.group10.koiauction.entity.Auction;
import com.group10.koiauction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionAPI {

    @Autowired
    private AuctionService auctionService;

    // Create a new auction
    @PostMapping
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
        Auction createdAuction = auctionService.createAuction(auction);
        return ResponseEntity.ok(createdAuction);
    }

    // Update an existing auction
    @PutMapping("/{id}")
    public ResponseEntity<Auction> updateAuction(@PathVariable("id") String auctionId, @RequestBody Auction auction) {
        auction.setAuctionId(auctionId);  // Set the ID to the incoming request
        Auction updatedAuction = auctionService.updateAuction(auction);
        return ResponseEntity.ok(updatedAuction);
    }

    // Soft delete an auction by setting its status to INACTIVE
    @DeleteMapping("/{id}")
    public ResponseEntity<Auction> deleteAuction(@PathVariable("id") String auctionId) {
        Auction deletedAuction = auctionService.deleteAuction(auctionId);
        return ResponseEntity.ok(deletedAuction);
    }

    // Get all auctions
    @GetMapping
    public ResponseEntity<List<Auction>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
        return ResponseEntity.ok(auctions);
    }

    // Get auction by ID
    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable("id") String auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        return ResponseEntity.ok(auction);
    }
}
