package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopBidderAmountDTO {
    private Long id;
    private String username;
    private double amount;

}
