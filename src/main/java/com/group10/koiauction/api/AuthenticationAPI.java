package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.request.RegisterAccountRequest;
import com.group10.koiauction.service.AuthenticationController;
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
    AuthenticationController authenticationController;
    @PostMapping("register")
    public ResponseEntity register(@Valid @RequestBody RegisterAccountRequest registerAccountRequest) {
            Account newAccount = authenticationController.register(registerAccountRequest);
            return ResponseEntity.ok(newAccount);
    }
}
