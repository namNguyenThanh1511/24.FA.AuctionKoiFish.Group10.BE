package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.Payment;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.WithDrawRequest;
import com.group10.koiauction.entity.enums.PaymentStatusEnum;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.entity.enums.TransactionStatus;
import com.group10.koiauction.entity.enums.WithDrawRequestEnum;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.request.ApproveWithDrawRequestDTO;
import com.group10.koiauction.model.request.WithDrawRequestDTO;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.model.response.BalanceResponseDTO;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.PaymentRepository;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.repository.WithDrawRequestRepository;
import com.group10.koiauction.utilities.AccountUtils;
import lombok.With;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountUtils accountUtils;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    WithDrawRequestRepository withDrawRequestRepository;


    public Transaction depositFunds2(String returnUrl) {
        Account account = accountUtils.getCurrentAccount();
        Map<String, String> params;
        try {
            params = extractParams(returnUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting parameters: " + e.getMessage());
        }

        String transactionId = params.get("transactionId");
        String vnpBankCode = params.get("vnp_BankCode");
        String vnpCardType = params.get("vnp_CardType");
        String vnpResponseCode = params.get("vnp_ResponseCode");

        Long transactionIdLong = Long.parseLong(transactionId);
        Transaction transaction = transactionRepository.findTransactionById(transactionIdLong);
        Payment payment = transaction.getPayment();
        payment.setBankCode(vnpBankCode);
        payment.setCardType(vnpCardType);

        switch (vnpResponseCode) {
            case "00":
                payment.setStatus(PaymentStatusEnum.SUCCESS);
                payment.setDescription("Payment successfully");
                if (transaction.getStatus().equals(TransactionStatus.PENDING)) {
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    account.setBalance(account.getBalance() + transaction.getAmount());
                } else {
                    throw new RuntimeException("Transaction has been processed");
                }
                break;
            case "11":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Payment timed out");
                transaction.setStatus(TransactionStatus.FAILED);
                break;
            case "24":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Payment cancelled");
                transaction.setStatus(TransactionStatus.FAILED);
                break;
            default:
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Unknown error occurred");
                transaction.setStatus(TransactionStatus.FAILED);
                break;
        }

        Set<Transaction> transactionSet = new HashSet<>();
        Transaction returnTransaction = transactionRepository.save(transaction);
        transactionSet.add(returnTransaction);
        payment.setTransactions(transactionSet);

        accountRepository.save(account);
        paymentRepository.save(payment);
        return returnTransaction;
    }

    public WithDrawRequest createWithDrawRequest(WithDrawRequestDTO withDrawRequestDTO) {
        WithDrawRequest withDrawRequest = new WithDrawRequest();
        Account account = accountUtils.getCurrentAccount();
        if(withDrawRequestDTO.getAmount() > account.getBalance()) {
            throw new RuntimeException("Amount exceeds balance");
        }
        withDrawRequest.setAmount(withDrawRequestDTO.getAmount());
        withDrawRequest.setBankAccountName(withDrawRequestDTO.getBankAccountName());
        withDrawRequest.setBankAccountNumber(withDrawRequestDTO.getBankAccountNumber());
        withDrawRequest.setBankName(withDrawRequestDTO.getBankName());
        withDrawRequest.setStatus(WithDrawRequestEnum.PENDING);
        withDrawRequest.setUser(accountUtils.getCurrentAccount());
        try {
            withDrawRequest = withDrawRequestRepository.save(withDrawRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return withDrawRequest;
    }

    public WithDrawRequest approveWithDrawRequest(Long withDrawRequestId , ApproveWithDrawRequestDTO approveWithDrawRequestDTO) {
        WithDrawRequest withDrawRequest =
                withDrawRequestRepository.findById(withDrawRequestId).orElseThrow(()->new RuntimeException("withdraw " +
                        "request not found"));
        Account receiver = withDrawRequest.getUser();
        Account staff = accountUtils.getCurrentAccount();
        try{
            withDrawRequest.setStatus(WithDrawRequestEnum.APPROVED);
            withDrawRequest.setStaff(staff);
            withDrawRequest.setResponseNote(approveWithDrawRequestDTO.getResponseNote());
            withDrawRequest.setImage_url(approveWithDrawRequestDTO.getImage_url());
            withDrawRequest = withDrawRequestRepository.save(withDrawRequest);
            Transaction transaction = new Transaction();
            transaction.setCreateAt(new Date());
            transaction.setType(TransactionEnum.WITHDRAW_SUCCESS);
            transaction.setAmount(withDrawRequest.getAmount());
            transaction.setFrom(withDrawRequest.getUser());
            transaction.setDescription("Withdraw (-) : "+withDrawRequest.getAmount());
            transaction.setWithdrawRequest(withDrawRequest);
            receiver.setBalance(receiver.getBalance() - withDrawRequest.getAmount());
            accountRepository.save(receiver);
            transactionRepository.save(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public WithDrawRequest rejectWithDrawRequest(Long withDrawRequestId ,
                                            ApproveWithDrawRequestDTO approveWithDrawRequestDTO) {
        WithDrawRequest withDrawRequest =
                withDrawRequestRepository.findById(withDrawRequestId).orElseThrow(()->new RuntimeException("withdraw " +
                        "request not found"));
        Account receiver = withDrawRequest.getUser();
        Account staff = accountUtils.getCurrentAccount();
        try{
            withDrawRequest.setStatus(WithDrawRequestEnum.REJECTED);
            withDrawRequest.setStaff(staff);
            withDrawRequest.setResponseNote(approveWithDrawRequestDTO.getResponseNote());
            withDrawRequest.setImage_url(approveWithDrawRequestDTO.getImage_url());
            withDrawRequest = withDrawRequestRepository.save(withDrawRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public BalanceResponseDTO getCurrentUserBalance() {
        Account account = accountUtils.getCurrentAccount();
        BalanceResponseDTO balanceResponseDTO = new BalanceResponseDTO();
        balanceResponseDTO.setBalance(account.getBalance());
        balanceResponseDTO.setId(account.getUser_id());
        return balanceResponseDTO;
    }

    private static Map<String, String> extractParams(String url) {
        Map<String, String> params = new HashMap<>();
        String queryString = url.split("\\?")[1];
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
    }
}
