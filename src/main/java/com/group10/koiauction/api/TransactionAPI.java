package com.group10.koiauction.api;

import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.model.response.TransactionResponseDTO;
import com.group10.koiauction.model.response.TransactionResponsePaginationDTO;
import com.group10.koiauction.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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

    @GetMapping("/filter-transactions")
    public ResponseEntity<TransactionResponsePaginationDTO> filterTransactions(
            @RequestParam(required = false) TransactionEnum transactionType,
            @RequestParam(required = false) Long fromUserId,
            @RequestParam(required = false) Long toUserId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) Long auctionSessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        TransactionResponsePaginationDTO response = transactionService.filterTransactions(
                transactionType, fromUserId, toUserId, startDate, endDate, minAmount, maxAmount, auctionSessionId, page, size);

        return ResponseEntity.ok(response);
    }
}
