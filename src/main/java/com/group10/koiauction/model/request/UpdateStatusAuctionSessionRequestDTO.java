package com.group10.koiauction.model.request;

import lombok.Data;

@Data
public class UpdateStatusAuctionSessionRequestDTO {
    private String status;
    private String note;
}
