package com.group10.koiauction.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuctionRequestUpdateDTO {
    private String responseNote;
    private String status;
}
