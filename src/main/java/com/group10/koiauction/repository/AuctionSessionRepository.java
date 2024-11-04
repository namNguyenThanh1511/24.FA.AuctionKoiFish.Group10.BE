package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.AuctionSessionType;
import com.group10.koiauction.entity.enums.KoiSexEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {

    @Query("SELECT U FROM AuctionSession U WHERE U.auctionSessionId = ?1")
    AuctionSession findAuctionSessionById(@Param("auction_session_id") Long id);

    @Query("SELECT a FROM AuctionSession a WHERE a.staff.user_id = :accountId")
    Page<AuctionSession> findAllByStaffAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT distinct a FROM AuctionSession a JOIN a.koiFish k JOIN k.varieties v WHERE " +
            "(a.auctionType = :auctionType OR :auctionType IS NULL) AND " +
            "(a.koiFish.sex = :sex OR :sex IS NULL) AND " +
            "(a.koiFish.account.username = :breederName OR :breederName IS NULL) AND " + // Use the correct field here
            "(:varieties IS NULL OR v.name IN :varieties) AND " +
            "(COALESCE(:minSizeCm, 0) <= a.koiFish.sizeCm AND (COALESCE(:maxSizeCm, 100) >= a.koiFish.sizeCm)) AND " +
            "(COALESCE(:minWeightKg, 0) <= a.koiFish.weightKg AND (COALESCE(:maxWeightKg, 100) >= a.koiFish.weightKg)" +
            ") AND " +
            "(a.status = :status OR :status IS NULL)")
    Page<AuctionSession> searchAuctionSessions(
            @Param("auctionType") AuctionSessionType auctionType,
            @Param("sex") KoiSexEnum sex,
            @Param("breederName") String breederName,
            @Param("varieties") Set<String> varietiesName,
            @Param("minSizeCm") Double minSizeCm,
            @Param("maxSizeCm") Double maxSizeCm,
            @Param("minWeightKg") Double minWeightKg,
            @Param("maxWeightKg") Double maxWeightKg,
            @Param("status") AuctionSessionStatus status,
            Pageable pageable);

    @Query("SELECT a FROM AuctionSession a " +
            "JOIN a.bidSet b " +
            "WHERE b.member.user_id = :userId " +
            "AND b.bidAt = (SELECT MAX(b2.bidAt) FROM Bid b2 WHERE b2.auctionSession.auctionSessionId = a.auctionSessionId AND b2.member.user_id = :userId)")
    Page<AuctionSession> findLatestBidAuctionSessionsByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT U FROM AuctionSession U  WHERE  U.title = :title")
    AuctionSession findAuctionSessionByTitle(@Param("title") String title);

    @Query("SELECT COUNT(a) FROM AuctionSession a WHERE a.status IN (:statuses) ")
    long countdAuctionSessionByStatus(List<AuctionSessionStatus> statuses);

    @Query("SELECT COUNT(a) FROM AuctionSession a WHERE a.status NOT IN (:statuses) ")
    long countdAuctionSessionExceptStatus(List<AuctionSessionStatus> statuses);

    @Query("SELECT a FROM AuctionSession a WHERE a.auctionSessionId = :auctionSessionId AND a.status = :status")
    Optional<AuctionSession> findByIdAndStatus(@Param("auctionSessionId") Long auctionSessionId, @Param("status") AuctionSessionStatus status);
}
