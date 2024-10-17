package com.group10.koiauction.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_profit")
public class SystemProfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    double balance;
    String description;
    Date date;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    Transaction transaction;


}
