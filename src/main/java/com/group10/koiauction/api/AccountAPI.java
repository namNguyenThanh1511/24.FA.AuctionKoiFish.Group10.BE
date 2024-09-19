package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.request.RegisterAccountRequest;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/account")
public class AccountAPI {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountService accountService;
    @GetMapping("/all")

    public ResponseEntity<List<Account>> getAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Account> deleteAccount(@PathVariable Long id) {
        Account deletedAccount = accountService.deleteAccount(id);
        return ResponseEntity.ok(deletedAccount);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id , @Valid @RequestBody RegisterAccountRequest account) {
        Account deletedAccount = accountService.updateAccount(id,account);
        return ResponseEntity.ok(deletedAccount);
    }
}
