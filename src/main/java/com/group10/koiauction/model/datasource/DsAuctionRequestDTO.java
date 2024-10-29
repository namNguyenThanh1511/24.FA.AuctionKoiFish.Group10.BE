package com.group10.koiauction.model.datasource;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DsAuctionRequestDTO {
    private String title;
    private String description;
    //    private Long breeder_id;
    private Long koiFish_id;
    private Date createdDate;
}
