package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.TransactionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private long id;
    private Date createAt;
    private TransactionEnum type;
    private double amount;
    private String description;
    private AuctionSessionResponseAccountDTO fromAccount;
    private AuctionSessionResponseAccountDTO toAccount;
    private long auctionSessionId;

}
