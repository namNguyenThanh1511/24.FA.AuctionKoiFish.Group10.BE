package com.group10.koiauction.api;

import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.model.response.TransactionResponseDTO;
import com.group10.koiauction.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class TransactionAPI {

    @Autowired
    TransactionService transactionService;

    @GetMapping()
    public ResponseEntity getAllTransaction() {
        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionById() {
        List<TransactionResponseDTO> transactions = transactionService.getMemberTransactions();
        return ResponseEntity.ok(transactions);
    }

}
