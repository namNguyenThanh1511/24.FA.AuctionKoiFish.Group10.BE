package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.request.AccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.repository.AccountRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationController {
    @Autowired
    AccountRepository accountRepository;
    public Account register(AccountRequest accountRequest) {
        try {
            Account account = new Account();
            account.setUsername(accountRequest.getUsername());
            account.setPassword(accountRequest.getPassword());
            account.setFirstName(accountRequest.getFirstName());
            account.setLastName(accountRequest.getLastName());
            account.setEmail(accountRequest.getEmail());
            account.setPhoneNumber(accountRequest.getPhoneNumber());
            account.setAddress(accountRequest.getAddress());
            return accountRepository.save(account);
        }catch (ConstraintViolationException e) {
            if (e.getMessage().contains(accountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(accountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            }
            throw e;
        }

    }
}
