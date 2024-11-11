package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionSession;
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
    Bid getLatestBidAmountOfCurrentMemberOfAuctionSession(@Param("member_id") Long memberId, @Param(
            "auction_session_id") Long auctionSessionId);

    @Query(value = "SELECT new com.group10.koiauction.model.MemberBidProjectionDTO(MAX(id), MAX(bidAmount),member" +
            ".user_id,auctionSession.auctionSessionId)" +
            "FROM Bid " +
            "WHERE auctionSession.auctionSessionId = :auctionSessionId " +
            "AND member.user_id != :winnerId " +
            "GROUP BY member.user_id , auctionSession.auctionSessionId")
    List<MemberBidProjectionDTO> findMaxBidForEachMemberInAuctionSessionExceptWinner(@Param("auctionSessionId") Long auctionSessionId,
                                                                                     @Param("winnerId") Long winnerId);

    @Query(value = "SELECT new com.group10.koiauction.model.MemberBidProjectionDTO(MAX(id), MAX(bidAmount),member" +
            ".user_id,auctionSession.auctionSessionId)" +
            "FROM Bid " +
            "WHERE auctionSession.auctionSessionId = :auctionSessionId " +
            "GROUP BY member.user_id , auctionSession.auctionSessionId")
    List<MemberBidProjectionDTO> findMaxBidForEachMemberInAuctionSession(@Param("auctionSessionId") Long auctionSessionId);

    @Query("SELECT b.auctionSession.auctionSessionId , COUNT(DISTINCT b.member) " +
            "FROM Bid b " +
            "GROUP BY b.auctionSession " +
            "ORDER BY COUNT(DISTINCT b.member) DESC " +
            "LIMIT :top")
    List<Object[]> findTopTrendingAuctionSession(@Param("top") int top);

    @Query("SELECT b.auctionSession.auctionSessionId , COUNT(b.id) " +
            "FROM Bid b " +
            "GROUP BY b.auctionSession " +
            "ORDER BY COUNT(b.id) DESC " +
            "LIMIT :top")
    List<Object[]> findTopAuctionSessionNumberOfBid(@Param("top") int top);

    @Query("SELECT a.user_id,a.username,MAX(b.bidAmount) " +
            "FROM Bid b " +
            "JOIN b.member a " +
            "GROUP BY b.member " +
            "ORDER BY MAX(b.bidAmount) DESC " +
            "LIMIT :top")
    List<Object[]> findTopBidderAmount(@Param("top") int top);

    @Query("SELECT a.user_id,a.username,COUNT (b.id) " +
            "FROM Bid b " +
            "JOIN b.member a " +
            "GROUP BY b.member " +
            "ORDER BY COUNT (b.id) DESC " +
            "LIMIT :top")
    List<Object[]> findTopBidderNumberOfBid(@Param("top") int top);


    @Query("SELECT COUNT(b.id) " +
            "FROM Bid b " +
            "GROUP BY b.auctionSession " +
            "ORDER BY COUNT(b.id) DESC  ")
    List<Long> findBidCountsPerSession();

    @Query("SELECT kv.id,kv.name,COUNT(distinct b.member) " +
            "FROM Bid b " +
            "JOIN b.auctionSession a " +
            "JOIN a.koiFish koi " +
            "JOIN koi.varieties kv " +
            "GROUP BY kv.id , kv.name " +
            "ORDER BY COUNT(distinct b.member) DESC " +
            "LIMIT :top")
    List<Object[]> findTopVarieties(@Param("top") int top);

    @Query("SELECT distinct b.member from Bid b WHERE b.auctionSession.auctionSessionId = :auctionSessionId ")
    Set<Account> getAllParticipantsOfAuctionSession(@Param("auctionSessionId") Long auctionSessionId);

//    @Query("SELECT AVG(bid_count) FROM " +
//            "( SELECT b.auctionSession.auctionSessionId , COUNT(b.id) as bid_count " +
//            "  FROM Bid b " +
//            "  GROUP BY b.auctionSession " +
//            "  ORDER BY COUNT(b.id) DESC )")
//    double calculateAvgBidCountPerAuctionSession();


    boolean existsByMemberAndAuctionSession(Account member, AuctionSession auctionSession);


}
