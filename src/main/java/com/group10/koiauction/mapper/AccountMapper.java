package com.group10.koiauction.mapper;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.model.response.AccountResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toAccount(LoginAccountRequest loginAccountRequest);
    Account toAccount(RegisterAccountRequest registerAccountRequest);
    AccountResponse toAccountResponse(LoginAccountRequest loginAccountRequest);
    AccountResponse toAccountResponse(RegisterAccountRequest registerAccountRequest);
    AccountResponse toAccountResponse(Account account);

}
