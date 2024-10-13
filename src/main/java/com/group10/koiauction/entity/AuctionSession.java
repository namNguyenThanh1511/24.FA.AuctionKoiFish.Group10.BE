package com.group10.koiauction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.AuctionSessionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "auction_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_session_id")
    private Long auctionSessionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "starting_price", nullable = false)
    private double startingPrice;

    @Column(name = "current_price")
    private double currentPrice;

    @Column(name = "buy_now_price")
    private double buyNowPrice;

    @Column(name = "bid_increment", nullable = false)
    private double bidIncrement;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", nullable = false)
    private AuctionSessionType auctionType;

    @Column(name = "min_balance_to_join")
    private double minBalanceToJoin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionSessionStatus status;

    private String note;

    Date createAt;

    Date updateAt;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Account winner;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Account staff;

//    @ManyToOne
//    @JoinColumn(name = "auction_id", nullable = false)
//    private Auction auction;
//
    @ManyToOne
    @JoinColumn(name = "koi_id", nullable = false)
    private KoiFish koiFish;


    @OneToOne
    @JoinColumn(name = "auction_request_id", nullable = false)
    private AuctionRequest auctionRequest;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Account manager;

    @OneToMany(mappedBy = "auctionSession")
    @JsonIgnore
    private Set<Bid> bidSet;

    @OneToOne(mappedBy = "auctionSession")
    Transaction transaction;




}
