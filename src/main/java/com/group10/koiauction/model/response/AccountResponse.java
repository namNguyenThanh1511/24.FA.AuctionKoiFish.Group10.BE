package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import lombok.Data;

@Data
public class AccountResponse {
    private long user_id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private AccountStatusEnum status;
    private AccountRoleEnum roleEnum;
    private String token;

}
