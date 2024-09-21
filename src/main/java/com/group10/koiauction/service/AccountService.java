package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account deleteAccount(Long id) {
        Account target = getAccountById(id);
        target.setStatus(AccountStatusEnum.INACTIVE);
        target.setUpdatedDate(new Date());
        return accountRepository.save(target);
    }

    public Account updateAccount(Long id, RegisterAccountRequest account) {
        try {
            Account target = getAccountById(id);
            target.setUsername(account.getUsername());
            target.setPassword(account.getPassword());
            target.setEmail(account.getEmail());
            target.setFirstName(account.getFirstName());
            target.setLastName(account.getLastName());
            target.setPassword(account.getPassword());
            target.setAddress(account.getAddress());
            target.setRoleEnum(getRoleEnumX(account.getRoleEnum()));
            target.setUpdatedDate(new Date());
            return accountRepository.save(target);
        }catch (Exception e) {
            if (e.getMessage().contains(account.getPhoneNumber())) {
                throw new DuplicatedEntity("Duplicated phone");
            } else if (e.getMessage().contains(account.getEmail())) {
                throw new DuplicatedEntity("Duplicated  email ");
            }
            throw e;
        }

    }

    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }
        return account;
    }
    public AccountRoleEnum getRoleEnumX(String role) {
        return switch (role.toLowerCase().trim()) {
            case "member" -> AccountRoleEnum.MEMBER;
            case "staff" -> AccountRoleEnum.STAFF;
            case "manager" -> AccountRoleEnum.MANAGER;
            case "koibreeder" -> AccountRoleEnum.KOI_BREEDER;
            default -> throw new EntityNotFoundException("Invalid role");
        };
    }
}
