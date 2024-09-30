package com.group10.koiauction.utilities;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccountUtils {

    @Autowired
    AccountRepository accountRepository;
    public Account getCurrentAccount() {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findByUser_id(account.getUser_id());
    }
}
