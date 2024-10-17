package com.group10.koiauction.entity;

import com.group10.koiauction.entity.enums.PaymentRequestEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_request")
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createAt;

    private Date updateAt;

    private Date dueDate;

    @Enumerated(EnumType.STRING)
    private PaymentRequestEnum status;

    private double winning_amount;

    private double final_amount;

    private double service_fee;

    @OneToOne(mappedBy = "paymentRequest")
    Payment payment;

    @OneToOne(mappedBy = "paymentRequest")
    Transaction transaction;

    @OneToOne
    @JoinColumn(name = "auction_session_id")
    AuctionSession auctionSession;

    @ManyToOne
    @JoinColumn(name = "member_id")
    Account member;

    @ManyToOne
    @JoinColumn(name = "koi_id")
    KoiFish koiFish;


}
