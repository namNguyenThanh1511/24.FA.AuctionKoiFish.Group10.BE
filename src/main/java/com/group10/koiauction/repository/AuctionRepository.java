package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Auction;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findById(@NotNull String auctionId);
}
