package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.response.AuctionSessionResponseAccountDTO;
import com.group10.koiauction.model.response.TransactionResponseDTO;
import com.group10.koiauction.repository.TransactionRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountMapper accountMapper;

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

}
