package com.group10.koiauction.service;

import com.group10.koiauction.entity.Auction;
import com.group10.koiauction.entity.enums.AuctionStatusEnum;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    // Create a new auction
    public Auction createAuction(Auction auction) {
        // Check for duplicate entity
        Optional<Auction> existingAuction = auctionRepository.findById(auction.getAuctionId());
        if (existingAuction.isPresent()) {
            throw new DuplicatedEntity("Auction already exists with ID: " + auction.getAuctionId());
        }
        return auctionRepository.save(auction);
    }

    // Update an existing auction
    public Auction updateAuction(Auction auction) {
        // Find auction by ID
        Auction existingAuction = auctionRepository.findById(auction.getAuctionId())
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + auction.getAuctionId()));

        // Update auction details
        existingAuction.setStartingBid(auction.getStartingBid());
        existingAuction.setCurrentBid(auction.getCurrentBid());
        existingAuction.setStartDate(auction.getStartDate());
        existingAuction.setEndDate(auction.getEndDate());
        existingAuction.setAuctionMethod(auction.getAuctionMethod());
        existingAuction.setStatus(auction.getStatus());

        return auctionRepository.save(existingAuction);
    }

    // Delete (by updating status to INACTIVE)
    public Auction deleteAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + auctionId));

        auction.setStatus(AuctionStatusEnum.CANCELLED);
        return auctionRepository.save(auction);
    }

    // Get all auctions
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    // Search auction by ID
    public Auction getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with ID: " + auctionId));
    }
}
