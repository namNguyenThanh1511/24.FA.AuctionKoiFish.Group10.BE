package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.request.LoginAccountRequest;
import com.group10.koiauction.entity.request.RegisterAccountRequest;
import com.group10.koiauction.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;
    @PostMapping("register")
    public ResponseEntity register(@Valid @RequestBody RegisterAccountRequest registerAccountRequest) {
            Account newAccount = authenticationService.register(registerAccountRequest);
            return ResponseEntity.ok(newAccount);
    }

    @PostMapping("login")
    public  ResponseEntity<String> login(@Valid @RequestBody LoginAccountRequest loginAccountRequest) {
        Account account = authenticationService.login(loginAccountRequest);
        return ResponseEntity.ok("login successful");
    }
}
