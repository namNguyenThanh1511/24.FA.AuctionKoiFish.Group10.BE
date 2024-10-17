package com.group10.koiauction.config;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.Payment;
import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.PaymentMethodEnum;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.model.request.DepositFundsRequest;
import com.group10.koiauction.repository.PaymentRepository;
import com.group10.koiauction.repository.TransactionRepository;
import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Configuration
public class VNPayConfig {

    private static final String LOCAL_PORT_FE = "5173";
    private static final String BASE_URL_LOCAL = "http://localhost:"+LOCAL_PORT_FE+"/";
    private static final String BASE_URL_HOST = "http://14.225.220.131/";
    private static final String BASE_URL_HOSTNAME = "http://www.koiauctionsystem.store/";

    @Autowired
    AccountUtils accountUtils;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    PaymentRepository paymentRepository;

    //get payment link -> create transaction
    public String createUrl(DepositFundsRequest fundsRequest) throws  Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        Payment payment = new Payment();
        payment.setAmount(fundsRequest.getAmount());
        payment.setCreateAt(new Date());
        payment.setMethod(PaymentMethodEnum.BANKING);


        Account member = accountUtils.getCurrentAccount();
        String uuidForFundRequest = UUID.randomUUID().toString();

        Set<Transaction> transactionSet = new HashSet<>();
        Transaction transaction = new Transaction();

        transaction.setCreateAt(new Date());
        transaction.setAmount(fundsRequest.getAmount());
        transaction.setType(TransactionEnum.PENDING);
        transaction.setTo(member);
        transaction.setDescription("Deposit funds");

        payment.setTransactions(transactionSet);
        transaction.setPayment(payment);

        transactionSet.add(transaction);
        paymentRepository.save(payment);
        Transaction transactionReturn = transactionRepository.save(transaction);

        double amount = fundsRequest.getAmount()*100;
        String amountStr = String.valueOf((int)amount);


        String tmnCode = "GVOIN1TF";
        String secretKey = "JE6NKK6CJRA7V35NCRU3RS2RCFTSJGH3";
        String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = BASE_URL_LOCAL+"member-profile/wallet/success?transactionId=" + transactionReturn.getId();
        //?fundRequestId=2
        //&vnp_Amount=20000000
        //&vnp_BankCode=NCB
        //&vnp_BankTranNo=VNP14612806
        //&vnp_CardType=ATM
        //&vnp_OrderInfo=Thanh+toan+cho+ma+GD%3A+857e6482-bd65-418b-b978-d2009d3e19b1
        //&vnp_PayDate=20241013234957
        //&vnp_ResponseCode=00
        //&vnp_TmnCode=GVOIN1TF
        //&vnp_TransactionNo=14612806
        //&vnp_TransactionStatus=00
        //&vnp_TxnRef=857e6482-bd65-418b-b978-d2009d3e19b1
        //&vnp_SecureHash=2cc9a879a21ba651b5b58a790b082d0ac73886caf6f5b873edbe850b6cbd02e3510b3f73a0192cc40467c2781e03df5fa1f05ca6a1e83fc82337437f142eb665

        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", uuidForFundRequest);
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + uuidForFundRequest);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount",amountStr);

        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "128.199.178.23");

        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(secretKey, signData);

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }
    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
