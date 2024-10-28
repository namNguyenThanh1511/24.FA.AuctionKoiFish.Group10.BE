package com.group10.koiauction.model;

import com.group10.koiauction.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberBidProjectionDTO {
    private Long id;
    private Double bidAmount;
    private long loser_id;
    private long auction_session_id;

}
