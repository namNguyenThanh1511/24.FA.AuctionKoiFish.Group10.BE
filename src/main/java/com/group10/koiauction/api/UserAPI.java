package com.group10.koiauction.api;

import com.group10.koiauction.config.VNPayConfig;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.model.request.DepositFundsRequest;
import com.group10.koiauction.model.response.BalanceResponseDTO;
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

    @PutMapping("depositFunds/{id}")
    public ResponseEntity depositFunds(@PathVariable Long id) throws Exception {
        Transaction transaction = userService.depositFunds(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("user/balance")
    public ResponseEntity getBalance() {
        BalanceResponseDTO balanceResponseDTO = userService.getCurrentUserBalance();
        return ResponseEntity.ok(balanceResponseDTO);
    }
}
