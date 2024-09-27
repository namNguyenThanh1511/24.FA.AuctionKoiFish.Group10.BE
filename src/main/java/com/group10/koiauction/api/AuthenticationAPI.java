package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.model.request.LoginAccountRequest;
import com.group10.koiauction.model.request.RegisterAccountRequest;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.service.AccountService;
import com.group10.koiauction.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    AccountService accountService;

    @PostMapping("register")
    public ResponseEntity register(@Valid @RequestBody RegisterAccountRequest registerAccountRequest) {
            AccountResponse newAccount = authenticationService.register(registerAccountRequest);
            return ResponseEntity.ok(newAccount);
    }

    @PostMapping("login")
    public  ResponseEntity login(@Valid @RequestBody LoginAccountRequest loginAccountRequest) {
        AccountResponse account = authenticationService.login(loginAccountRequest);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/deleteDB/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        return ResponseEntity.ok(authenticationService.deleteDB(id));
    }
    @GetMapping("/account/all")

    public ResponseEntity<List<Account>> getAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
    @GetMapping("/account/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse account = accountService.getAccountResponseById(id);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/account/{id}")
    public ResponseEntity<Account> deleteAccount(@PathVariable Long id) {
        Account deletedAccount = accountService.deleteAccount(id);
        return ResponseEntity.ok(deletedAccount);
    }
    @PutMapping("/account/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id , @Valid @RequestBody RegisterAccountRequest account) {
        Account deletedAccount = accountService.updateAccount(id,account);
        return ResponseEntity.ok(deletedAccount);
    }


}
