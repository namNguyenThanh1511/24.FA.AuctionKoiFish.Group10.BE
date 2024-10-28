package com.group10.koiauction.model.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DsBidRequestDTO {
        private double bidAmount;
        private Long auctionSessionId;
        private Long memberId;
}
