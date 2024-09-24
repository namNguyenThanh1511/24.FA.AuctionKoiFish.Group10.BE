package com.group10.koiauction.repository;

import com.group10.koiauction.entity.AuctionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRequestRepository extends JpaRepository<AuctionRequest, Long> {
    @Query("SELECT U FROM AuctionRequest U WHERE U.auction_request_id = ?1")
    AuctionRequest findByAuctionRequestId(@Param("auction_request_id") Long id);
}
