package com.group10.koiauction.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bid")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;
    private double bidAmount;
    private Date bidAt;
    private boolean isWinningBid;
    private double maxAutoBidAmount;//hạn mức để ngừng tự động trừ tiền khi đấu giá tự động
    private boolean isAutoBid;

    @ManyToOne
    @JoinColumn(name = "auction_session_id")
    private AuctionSession auctionSession;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Account member;

    @OneToOne(mappedBy = "bid")
    Transaction transaction;


}
