package com.group10.koiauction.repository;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {

    @Query("SELECT U FROM AuctionSession U WHERE U.auctionSessionId = ?1")
    AuctionSession findAuctionSessionById(@Param("auction_session_id") Long id);

    @Query("SELECT a FROM AuctionSession a WHERE a.staff.user_id = :accountId")
    Page<AuctionSession> findAllByStaffAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
