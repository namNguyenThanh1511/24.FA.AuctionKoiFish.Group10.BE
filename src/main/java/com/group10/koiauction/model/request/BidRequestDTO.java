package com.group10.koiauction.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class BidRequestDTO {
    private double bidAmount;
    private Long auctionSessionId;
}
