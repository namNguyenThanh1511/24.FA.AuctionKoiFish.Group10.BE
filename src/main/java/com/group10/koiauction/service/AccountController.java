package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountController {
    @Autowired
    AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account deleteAccount(Long id) {
        Account target = getAccountById(id);
        target.setStatus(AccountStatusEnum.INACTIVE);
        return accountRepository.save(target);
    }

    public Account updateAccount(Long id, Account account) {
        Account target = getAccountById(id);
        target.setUsername(account.getUsername());
        target.setPassword(account.getPassword());
        target.setEmail(account.getEmail());
        target.setFirstName(account.getFirstName());
        target.setLastName(account.getLastName());
        target.setPassword(account.getPassword());
        target.setAddress(account.getAddress());
        return accountRepository.save(target);
    }

    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }
        return account;
    }
}
