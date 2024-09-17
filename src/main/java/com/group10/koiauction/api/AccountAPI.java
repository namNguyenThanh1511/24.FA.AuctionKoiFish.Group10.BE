package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/account")
public class AccountAPI {
    @Autowired
    AccountRepository accountRepository;
    @GetMapping("getAllAccounts")

    public ResponseEntity<List<Account>> getAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return ResponseEntity.ok(accounts);
    }
}
