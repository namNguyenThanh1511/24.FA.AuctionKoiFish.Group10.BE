package com.group10.koiauction.repository;

import com.group10.koiauction.entity.AuctionSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {
}
