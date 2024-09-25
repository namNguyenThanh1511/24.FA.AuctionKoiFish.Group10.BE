package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
public class AuctionRequestResponse {
    private Long auction_request_id;
    private String title;
    private Date createdDate;
    private String description;
    private String responseNote;
    private AuctionRequestStatusEnum status;
    private Long breeder_id;
    private Long koi_id;
}
