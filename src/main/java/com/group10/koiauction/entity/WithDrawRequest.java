package com.group10.koiauction.entity;

import com.group10.koiauction.entity.enums.WithDrawRequestEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WithDrawRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bankAccountNumber;
    private String bankName;
    private String bankAccountName;
    private double amount;
    private String responseNote;
    private String image_url;

    @Enumerated(EnumType.STRING)
    private WithDrawRequestEnum status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    Account user;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    Account staff;

    @OneToOne(mappedBy = "withdrawRequest",cascade = CascadeType.ALL)
    Transaction transaction;


}
