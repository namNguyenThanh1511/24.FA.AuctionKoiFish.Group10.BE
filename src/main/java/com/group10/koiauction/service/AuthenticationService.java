package com.group10.koiauction.service;


import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.request.LoginAccountRequest;
import com.group10.koiauction.entity.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
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
            account.setRoleEnum(getRoleEnum(registerAccountRequest.getRoleEnum()));
            return accountRepository.save(account);
        } catch (Exception e) {
            if (e.getMessage().contains(registerAccountRequest.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(registerAccountRequest.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            } else if (e.getMessage().contains(registerAccountRequest.getUsername())) {
                throw new DuplicatedEntity("username  exist");

            }
            throw e;
        }

    }

    public Account login(LoginAccountRequest loginAccountRequest) {

        Account account = accountRepository.findByUsernameAndPassword(loginAccountRequest.getUsername(), loginAccountRequest.getPassword());
        if (account == null) {
            throw new EntityNotFoundException("Username or password are incorrect");
        }
        return account;

    }

    public AccountRoleEnum getRoleEnum(String role) {
        return switch (role.toLowerCase()) {
            case "member" -> AccountRoleEnum.MEMBER;
            case "staff" -> AccountRoleEnum.STAFF;
            case "manager" -> AccountRoleEnum.MANAGER;
            case "koi_breeder" -> AccountRoleEnum.KOI_BREEDER;
            default -> throw new EntityNotFoundException("Invalid role");
        };
    }
}
