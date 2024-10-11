package com.group10.koiauction.model.request;

import lombok.Data;

@Data
public class BuyNowRequestDTO {
    private double price;
    private Long auctionSessionId;
}
