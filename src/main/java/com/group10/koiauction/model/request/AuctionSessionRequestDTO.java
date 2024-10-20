package com.group10.koiauction.model.request;

import com.group10.koiauction.entity.enums.AuctionSessionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class AuctionSessionRequestDTO {
    private String title;

    private double startingPrice;

    private double buyNowPrice;

    private double bidIncrement;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private AuctionSessionType auctionType;

    private double minBalanceToJoin;

    private Long auction_request_id;

    private Long staff_id;
}
