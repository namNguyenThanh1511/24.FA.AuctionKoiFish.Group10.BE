package com.group10.koiauction.mapper;

import com.group10.koiauction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BidRepository extends JpaRepository<Bid, Long> {
    @Query(value = "SELECT MAX(bid_amount) FROM koiauctionsystem.bid WHERE member_id = :memberId AND " +
            "auction_session_id = :auctionSessionId",
            nativeQuery =
            true)
    double getMaxBidAmountByMemberId(@Param("memberId") Long memberId , @Param("auctionSessionId")Long auctionSessionId);

    @Query(value = "SELECT MAX(bid_amount) FROM koiauctionsystem.bid WHERE " +
            "auction_session_id = :auctionSessionId",
            nativeQuery =
                    true)
    double getMaxBidAmountByAuctionSessionId(@Param("auctionSessionId")Long auctionSessionId);


}
