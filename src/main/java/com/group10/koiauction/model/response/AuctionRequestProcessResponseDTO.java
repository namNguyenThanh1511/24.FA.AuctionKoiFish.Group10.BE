package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuctionRequestProcessResponseDTO {
    private Long id;
    private Date date;
    private AuctionRequestStatusEnum status;
    private AuctionRequestResponse auctionRequest;
    private AuctionSessionResponseAccountDTO manager;
    private AuctionSessionResponseAccountDTO staff;

}
