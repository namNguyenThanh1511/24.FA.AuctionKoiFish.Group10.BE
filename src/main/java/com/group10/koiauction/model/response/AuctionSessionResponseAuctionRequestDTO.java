package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class AuctionSessionResponseAuctionRequestDTO {
    private Long auction_request_id;
    private String title;
    private Date createdDate;
    private String description;
    private String response_note;
    private AuctionRequestStatusEnum status;
}
