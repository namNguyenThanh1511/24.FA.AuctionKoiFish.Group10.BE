package com.group10.koiauction.model.request;

import com.group10.koiauction.entity.enums.AuctionSessionType;
import lombok.Data;

import java.util.Date;

@Data

public class AuctionSessionRequestDTO {
    private String title;

    private double startingPrice;

    private double buyNowPrice;

    private double bidIncrement;

    private Date startDate;

    private Date endDate;

    private AuctionSessionType auctionType;

    private double minBalanceToJoin;

    private Long auction_request_id;
}
