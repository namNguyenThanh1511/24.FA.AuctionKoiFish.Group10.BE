package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.model.response.AuctionSessionResponseAccountDTO;
import com.group10.koiauction.model.response.TransactionResponseDTO;
import com.group10.koiauction.model.response.TransactionResponsePaginationDTO;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    AccountUtils accountUtils;

    public List<TransactionResponseDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<TransactionResponseDTO> transactionResponseDTOs = new ArrayList<>();
        for (Transaction transaction : transactions) {
            TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();
            transactionResponseDTO.setId(transaction.getId());
            transactionResponseDTO.setCreateAt(transaction.getCreateAt());
            transactionResponseDTO.setType(transaction.getType());
            transactionResponseDTO.setAmount(transaction.getAmount());
            transactionResponseDTO.setDescription(transaction.getDescription());

            Account from = transaction.getFrom();
            Account to = transaction.getTo();
            AuctionSessionResponseAccountDTO fromAccountResponse = getAuctionSessionResponseAccountDTO(from);
            AuctionSessionResponseAccountDTO toAccountResponse = getAuctionSessionResponseAccountDTO(to);

            transactionResponseDTO.setFromAccount(fromAccountResponse);
            transactionResponseDTO.setToAccount(toAccountResponse);
            transactionResponseDTO.setAuctionSessionId(transaction.getBid().getAuctionSession().getAuctionSessionId());
            transactionResponseDTOs.add(transactionResponseDTO);
        }
        return transactionResponseDTOs;
    }
    public AuctionSessionResponseAccountDTO getAuctionSessionResponseAccountDTO(Account account){
        if(account == null){
            return  null;
        }else{
            AuctionSessionResponseAccountDTO accountResponseDTO = accountMapper.toAuctionSessionResponseAccountDTO(account);
            accountResponseDTO.setId(account.getUser_id());
            accountResponseDTO.setFullName(account.getFirstName() + " " + account.getLastName());
            return accountResponseDTO;
        }

    }

    public List<TransactionResponseDTO> getMemberTransactions() {
        Long currentUserId = accountUtils.getCurrentAccount().getUser_id(); // Fetch the current user's ID
        List<Transaction> transactions = transactionRepository.findTransactionByUserId(currentUserId); // Query transactions

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for user ID: " + currentUserId);
            return new ArrayList<>(); // Return an empty list if no transactions
        }

        List<TransactionResponseDTO> transactionResponseDTOs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();
            transactionResponseDTO.setId(transaction.getId());
            transactionResponseDTO.setCreateAt(transaction.getCreateAt());
            transactionResponseDTO.setType(transaction.getType());
            transactionResponseDTO.setAmount(transaction.getAmount());
            transactionResponseDTO.setDescription(transaction.getDescription());

            // Set From and To accounts based on whether currentAccount is 'from' or 'to'
            Account currentAccount = accountUtils.getCurrentAccount();
            Account from = transaction.getFrom();
            Account to = transaction.getTo();

            // If currentAccount is the 'from' account
            if (from != null && from.getUser_id() == (currentAccount.getUser_id())) {
                AuctionSessionResponseAccountDTO fromAccountResponse = getAuctionSessionResponseAccountDTO(from);
                transactionResponseDTO.setFromAccount(fromAccountResponse);
            }

            // If currentAccount is the 'to' account
            if (to != null && to.getUser_id() == (currentAccount.getUser_id())) {
                AuctionSessionResponseAccountDTO toAccountResponse = getAuctionSessionResponseAccountDTO(to);
                transactionResponseDTO.setToAccount(toAccountResponse);
            }

            // Set auction session ID if available
            if (transaction.getAuctionSession() != null) {
                transactionResponseDTO.setAuctionSessionId(transaction.getAuctionSession().getAuctionSessionId());
            }

            // Set bid-related auction session ID if available
            if (transaction.getBid() != null && transaction.getBid().getAuctionSession() != null) {
                transactionResponseDTO.setAuctionSessionId(transaction.getBid().getAuctionSession().getAuctionSessionId());
            }

            transactionResponseDTOs.add(transactionResponseDTO); // Add the DTO to the list
        }

        return transactionResponseDTOs; // Return the transaction response list
    }

    public TransactionResponsePaginationDTO filterTransactions(
            TransactionEnum transactionType,
            Long fromUserId,
            Long toUserId,
            Date startDate,
            Date endDate,
            Double minAmount,
            Double maxAmount,
            Long auctionSessionId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> filteredTransactions = transactionRepository.filterTransactions(
                transactionType, fromUserId, toUserId, startDate, endDate, minAmount, maxAmount, auctionSessionId, pageable);
        List<TransactionResponseDTO> transactionResponseList = new ArrayList<>();
        for (Transaction transaction : filteredTransactions.getContent()) {
            TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();
            transactionResponseDTO.setId(transaction.getId());
            transactionResponseDTO.setCreateAt(transaction.getCreateAt());
            transactionResponseDTO.setType(transaction.getType());
            transactionResponseDTO.setAmount(transaction.getAmount());
            transactionResponseDTO.setDescription(transaction.getDescription());
            // Set From Account
            Account from = transaction.getFrom();
            if (from != null) {
                transactionResponseDTO.setFromAccount(getAuctionSessionResponseAccountDTO(from));
            }
            // Set To Account
            Account to = transaction.getTo();
            if (to != null) {
                transactionResponseDTO.setToAccount(getAuctionSessionResponseAccountDTO(to));
            }
            // Set auction session ID if available
            if (transaction.getAuctionSession() != null) {
                transactionResponseDTO.setAuctionSessionId(transaction.getAuctionSession().getAuctionSessionId());
            }
            transactionResponseList.add(transactionResponseDTO);
        }
        // Create TransactionResponsePaginationDTO and set pagination details
        TransactionResponsePaginationDTO response = new TransactionResponsePaginationDTO();
        response.setTransactionResponseList(transactionResponseList);
        response.setPageNumber(filteredTransactions.getNumber());
        response.setTotalPages(filteredTransactions.getTotalPages());
        response.setTotalElements(filteredTransactions.getTotalElements());
        return response;
    }

}
