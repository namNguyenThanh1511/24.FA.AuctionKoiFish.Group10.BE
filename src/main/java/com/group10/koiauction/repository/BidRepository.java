package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Bid;
import com.group10.koiauction.model.MemberBidProjectionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BidRepository extends JpaRepository<Bid, Long> {
    @Query(value = "SELECT MAX(bid_amount) FROM koiauctionsystem.bid WHERE member_id = :memberId AND " +
            "auction_session_id = :auctionSessionId",
            nativeQuery =
                    true)
    double getMaxBidAmountByMemberId(@Param("memberId") Long memberId, @Param("auctionSessionId") Long auctionSessionId);

    @Query(value = "SELECT MAX(bid_amount) FROM koiauctionsystem.bid WHERE " +
            "auction_session_id = :auctionSessionId",
            nativeQuery =
                    true)
    double getMaxBidAmountByAuctionSessionId(@Param("auctionSessionId") Long auctionSessionId);

    @Query(value = "SELECT  * FROM koiauctionsystem.bid WHERE member_id = :member_id AND auction_session_id = :auction_session_id " +
            "ORDER BY bid_amount DESC LIMIT 1;",
            nativeQuery = true)
    Bid getLatestBidAmountOfCurrentMemberOfAuctionSession(@Param("member_id") Long memberId , @Param(
            "auction_session_id") Long auctionSessionId);

    @Query(value = "SELECT new com.group10.koiauction.model.MemberBidProjectionDTO(MAX(id), MAX(bidAmount),member" +
            ".user_id,auctionSession.auctionSessionId)" +
            "FROM Bid " +
            "WHERE auctionSession.auctionSessionId = :auctionSessionId " +
            "AND member.user_id != :winnerId " +
            "GROUP BY member.user_id , auctionSession.auctionSessionId")
    List<MemberBidProjectionDTO> findMaxBidForEachMemberInAuctionSessionExceptWinner(@Param("auctionSessionId") Long auctionSessionId,
                                                                                     @Param("winnerId") Long winnerId);


}
