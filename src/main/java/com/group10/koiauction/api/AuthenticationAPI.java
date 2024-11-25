package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.model.request.*;
import com.group10.koiauction.model.response.*;
import com.group10.koiauction.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @PostMapping("login-google")
    public ResponseEntity loginGG(@RequestBody LoginGoogleRequestDTO loginGoogleRequestDTO) {
        AccountResponse account = authenticationService.loginGoogle(loginGoogleRequestDTO.getToken());
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

    @PutMapping("/account/unlock/{id}")
    public ResponseEntity<AccountResponseForManageDTO> unlockAccount(@PathVariable Long id) {
        AccountResponseForManageDTO account = authenticationService.unlockAccount(id);
        return ResponseEntity.ok(account);
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

    @PostMapping("/manager/create-staff-account")
    public ResponseEntity<AccountResponse> createStaffAccount(@RequestBody CreateStaffAccountRequest createStaffAccountRequest) {
        AccountResponse accountResponse = authenticationService.createStaffAccount(createStaffAccountRequest);
        return ResponseEntity.ok(accountResponse);
    }


    @PostMapping("/forgot-password")
    public  ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authenticationService.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("forgot password successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        authenticationService.resetPassword(resetPasswordRequestDTO);
        return ResponseEntity.ok("Password reset successfully");
    }

    @GetMapping("/breeders")
    public List<Account> getBreederAccounts() {
        return authenticationService.getAllBreederAccounts();
    }

//    @GetMapping("/staffs")
//    public List<Account> getStaffAccounts(){
//        return authenticationService.getAllStaffAccounts();
//    }

    @GetMapping("/staffs")
    public ResponseEntity<List<AuctionSessionResponseAccountDTO>> getStaffAccounts(){
        List<AuctionSessionResponseAccountDTO> response = authenticationService.getAllStaffAccountsWithShorterResponse();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/breederSimplified")
    public ResponseEntity<List<AccountResponseSimplifiedDTO>> getKoiBreederSimplifiedAccounts(){
        List<AccountResponseSimplifiedDTO> response = authenticationService.getAllKoiBreederSimplifiedAccounts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members")
    public List<Account> getMemberAccounts(){
        return authenticationService.getAllMemberAccounts();
    }

    @GetMapping("/breeders-pagination")
    public ResponseEntity<AccountResponseForManagePagination> getBreederAccountsPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AccountResponseForManagePagination breeders = authenticationService.getAllBreederAccountsPagination(page, size);
        return ResponseEntity.ok(breeders);
    }

    @GetMapping("/staffs-pagination")
    public ResponseEntity<AccountResponseForManagePagination> getStaffAccountsPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AccountResponseForManagePagination staffs = authenticationService.getAllStaffAccountsPagination(page, size);
        return ResponseEntity.ok(staffs);
    }

    @GetMapping("/members-pagination")
    public ResponseEntity<AccountResponseForManagePagination> getMemberAccountsPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AccountResponseForManagePagination members = authenticationService.getAllMemberAccountsPagination(page, size);
        return ResponseEntity.ok(members);
    }
    @PatchMapping("/account/fcm")
    public ResponseEntity updateFCM(@RequestBody UpdateFCMRequestDTO updateFCMRequestDTO) {
        AccountResponse response = authenticationService.updateFCM(updateFCMRequestDTO);
        return ResponseEntity.ok(response);
    }
}
