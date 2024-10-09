package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.AuctionSessionType;
import lombok.Data;

import java.util.Date;
@Data
public class AuctionSessionResponseDTO {

    private Long auctionSessionId;

    private String title;

    private double startingPrice;

    private double currentPrice;

    private double buyNowPrice;

    private double bidIncrement;

    private Date startDate;

    private Date endDate;

    private double minBalanceToJoin;

    private Long koi_id;

    private Long auction_request_id;

    private Long winner_id;

    private Long staff_id;

    private Long manager_id;

    private AuctionSessionType auctionType;

    private AuctionSessionStatus auctionStatus;

}
