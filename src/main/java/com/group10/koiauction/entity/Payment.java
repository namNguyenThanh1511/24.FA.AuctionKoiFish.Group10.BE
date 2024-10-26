package com.group10.koiauction.entity;

import com.group10.koiauction.entity.enums.PaymentMethodEnum;
import com.group10.koiauction.entity.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum method;
    private Date createAt;
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;
    private String bankCode;
    private String cardType;
    private String description;

    @OneToOne
    @JoinColumn(name = "payment_request_id")
    PaymentRequest paymentRequest;

    @OneToMany(mappedBy = "payment",cascade = CascadeType.ALL)//save transaction when save payment
    Set<Transaction> transactions;

}
