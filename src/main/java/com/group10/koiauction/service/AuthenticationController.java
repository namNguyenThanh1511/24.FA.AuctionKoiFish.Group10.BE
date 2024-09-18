package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.repository.AccountRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationController {
    @Autowired
    AccountRepository accountRepository;
    public Account register(RegisterAccountRequest registerAccountRequest) {
        try {
            Account account = new Account();
            account.setUsername(registerAccountRequest.getUsername());
            account.setPassword(registerAccountRequest.getPassword());
            account.setFirstName(registerAccountRequest.getFirstName());
            account.setLastName(registerAccountRequest.getLastName());
            account.setEmail(registerAccountRequest.getEmail());
            account.setPhoneNumber(registerAccountRequest.getPhoneNumber());
            account.setAddress(registerAccountRequest.getAddress());
            return accountRepository.save(account);
        }catch (Exception e) {
            if (e.getMessage().contains(registerAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(registerAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            }
            throw e;
        }

    }
}
