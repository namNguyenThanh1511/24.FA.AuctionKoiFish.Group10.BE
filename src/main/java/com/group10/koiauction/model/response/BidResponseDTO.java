package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidResponseDTO {
    private Long id;
    private double bidAmount;
    private Date bidAt;
    private boolean isAutoBid;
    private AuctionSessionResponseAccountDTO member;
    private Long auctionSessionId;
}
