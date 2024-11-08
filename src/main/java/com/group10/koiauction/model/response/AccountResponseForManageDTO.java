package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseForManageDTO {

    private long user_id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private AccountStatusEnum status;
    private AccountRoleEnum roleEnum;
    private double balance;
    private Date createdAt;
    private Date updatedAt;


}
