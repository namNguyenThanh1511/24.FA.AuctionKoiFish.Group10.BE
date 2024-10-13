package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import lombok.Data;

@Data
public class AuctionSessionResponseAccountDTO {
    private Long id;
    private String username;
    private String fullName;
}
