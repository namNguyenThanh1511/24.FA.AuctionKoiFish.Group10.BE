package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.model.request.*;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("register")
    public ResponseEntity register(@Valid @RequestBody RegisterAccountRequest registerAccountRequest) {
            AccountResponse newAccount = authenticationService.register(registerAccountRequest);
            return ResponseEntity.ok(newAccount);
    }


    @PostMapping("register-member")
    public ResponseEntity registerMember(@Valid @RequestBody RegisterMemberRequest registerAccountRequest) {
        AccountResponse newAccount = authenticationService.registerMember(registerAccountRequest);
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
        List<Account> accounts = authenticationService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
    @GetMapping("/account/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse account = authenticationService.getAccountResponseById(id);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/account/{id}")
    public ResponseEntity<Account> deleteAccount(@PathVariable Long id) {
        Account deletedAccount = authenticationService.deleteAccount(id);
        return ResponseEntity.ok(deletedAccount);
    }
    @PutMapping("/account/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id , @Valid @RequestBody RegisterAccountRequest account) {
        Account deletedAccount = authenticationService.updateAccount(id,account);
        return ResponseEntity.ok(deletedAccount);
    }
    @GetMapping("/account/profile")
    public ResponseEntity<AccountResponse> getAccountProfile() {
        AccountResponse accountResponse = authenticationService.getAccountProfile();
        return ResponseEntity.ok(accountResponse);
    }
    @PutMapping("/account/update-profile/{id}")
    public ResponseEntity<AccountResponse> updateAccountProfile(@PathVariable Long id ,
     @Valid @RequestBody                                                           UpdateProfileRequestDTO updateProfileRequestDTO){
        AccountResponse accountResponse = authenticationService.updateAccountProfile(id,updateProfileRequestDTO);
        return ResponseEntity.ok(accountResponse);
    }

    @PutMapping("/account/update-profile-current-user")
    public ResponseEntity<AccountResponse> updateAccountProfileOfCurrentUser(@Valid @RequestBody UpdateProfileRequestDTO updateProfileRequestDTO){
        AccountResponse accountResponse = authenticationService.updateAccountProfileOfCurrentUser(updateProfileRequestDTO);
        return ResponseEntity.ok(accountResponse);
    }



    @PostMapping("/manager/create-breeder-account")
    public ResponseEntity<AccountResponse> createBreederAccount(@RequestBody CreateBreederAccountRequest createBreederAccountRequest) {
        AccountResponse accountResponse = authenticationService.createBreederAccount(createBreederAccountRequest);
        return ResponseEntity.ok(accountResponse);
    }


    @PostMapping("forgot-password")
    public  ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authenticationService.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("forgot password successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        authenticationService.resetPassword(resetPasswordRequestDTO);
        return ResponseEntity.ok("Password reset successfully");
    }

}
