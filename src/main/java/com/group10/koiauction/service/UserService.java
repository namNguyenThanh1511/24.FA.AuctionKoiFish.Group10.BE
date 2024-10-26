package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.Payment;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.PaymentStatusEnum;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.entity.enums.TransactionStatus;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.model.response.BalanceResponseDTO;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.PaymentRepository;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                payment.setDescription("Giao dịch thành công");
                if (transaction.getStatus().equals(TransactionStatus.PENDING)) {
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    account.setBalance(account.getBalance() + transaction.getAmount());
                }else{
                    throw new RuntimeException("Transaction has been processed");
                }
                break;
            case "09":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "10":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "11":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "12":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Thẻ/Tài khoản của khách hàng bị khóa");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "13":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Sai mật khẩu xác thực giao dịch (OTP). Vui lòng thực hiện lại giao dịch");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "24":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Khách hàng hủy giao dịch");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "51":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Tài khoản của quý khách không đủ số dư để thực hiện giao dịch");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "65":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Tài khoản của quý khách đã vượt quá hạn mức giao dịch trong ngày");
                transaction.setStatus(TransactionStatus.FAILED);
                break;

            case "75":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("Ngân hàng thanh toán đang bảo trì");
                transaction.setStatus(TransactionStatus.FAILED);
                break;
            case "79":
                payment.setStatus(PaymentStatusEnum.FAILED);
                payment.setDescription("KH nhập sai mật khẩu thanh toán quá số lần quy định. Vui lòng thực hiện lại giao dịch");
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
