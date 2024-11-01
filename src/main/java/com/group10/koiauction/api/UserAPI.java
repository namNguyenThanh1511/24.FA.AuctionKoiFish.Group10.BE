package com.group10.koiauction.api;

import com.group10.koiauction.config.VNPayConfig;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.WithDrawRequest;
import com.group10.koiauction.model.request.ApproveWithDrawRequestDTO;
import com.group10.koiauction.model.request.DepositFundsRequest;
import com.group10.koiauction.model.request.DepositFundsRequestDTO;
import com.group10.koiauction.model.request.WithDrawRequestDTO;
import com.group10.koiauction.model.response.BalanceResponseDTO;
import com.group10.koiauction.model.response.WithDrawRequestResponsePaginationDTO;
import com.group10.koiauction.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@CrossOrigin("*")
@SecurityRequirement(name = "api")// để sử dụng token tren swagger
public class UserAPI {

    @Autowired
    VNPayConfig vnPayConfig;

    @Autowired
    UserService userService;


    @PostMapping("paymentURL/vn-pay")
    public ResponseEntity getPaymentURL(@RequestBody DepositFundsRequest fundsRequest) throws Exception {

        String paymentUrl = vnPayConfig.createUrl(fundsRequest);
        return ResponseEntity.ok(paymentUrl);

    }

//    @PutMapping("depositFunds/{id}")
//    public ResponseEntity depositFunds(@PathVariable Long id) throws Exception {
//        Transaction transaction = userService.depositFunds(id);
//        return ResponseEntity.ok(transaction);
//    }

    @PutMapping("depositFunds")
    public ResponseEntity depositFunds2(@RequestBody DepositFundsRequestDTO depositFundsRequestDTO) throws Exception {
        Transaction transaction = userService.depositFunds2(depositFundsRequestDTO.getReturnURL());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("withDraw")
    public ResponseEntity withDraw(@RequestBody WithDrawRequestDTO withDrawRequestDTO){
        WithDrawRequest withDrawRequest = userService.createWithDrawRequest(withDrawRequestDTO);
        return ResponseEntity.ok(withDrawRequest);
    }

    @PutMapping("withDraw/approve/{id}")
    public ResponseEntity approveWithDrawRequest(@PathVariable("id") Long id, @RequestBody ApproveWithDrawRequestDTO approveWithDrawRequestDTO){
        WithDrawRequest withDrawRequest = userService.approveWithDrawRequest(id,approveWithDrawRequestDTO);
        return ResponseEntity.ok(withDrawRequest);
    }

    @PutMapping("withDraw/reject/{id}")
    public ResponseEntity rejectWithDrawRequest(@PathVariable("id") Long id, @RequestBody ApproveWithDrawRequestDTO approveWithDrawRequestDTO){
        WithDrawRequest withDrawRequest = userService.rejectWithDrawRequest(id,approveWithDrawRequestDTO);
        return ResponseEntity.ok(withDrawRequest);
    }

    @GetMapping("withDrawRequest/pagination")
    public ResponseEntity getWithDrawRequestPagination(@RequestParam int page , @RequestParam int size , @RequestParam(required = false) Long userId){
        WithDrawRequestResponsePaginationDTO withDrawRequestResponsePaginationDTO = userService.getWithDrawRequestPagination(page,size,userId);
        return ResponseEntity.ok(withDrawRequestResponsePaginationDTO);
    }

    @GetMapping("withDrawRequest/currentUser/pagination")
    public ResponseEntity getWithDrawRequestOfCurrentUserPagination(@RequestParam int page , @RequestParam int size){
        WithDrawRequestResponsePaginationDTO withDrawRequestResponsePaginationDTO = userService.getWithDrawRequestOfCurrentUserPagination(page,size);
        return ResponseEntity.ok(withDrawRequestResponsePaginationDTO);
    }

    @GetMapping("user/balance")
    public ResponseEntity getBalance() {
        BalanceResponseDTO balanceResponseDTO = userService.getCurrentUserBalance();
        return ResponseEntity.ok(balanceResponseDTO);
    }
}
