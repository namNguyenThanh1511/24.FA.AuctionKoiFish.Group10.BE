package com.group10.koiauction.entity;


import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
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
@Table(name = "auction_request_process")
public class AuctionRequestProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date date;

    @Enumerated(EnumType.STRING)
    private AuctionRequestStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "auction_request_id")
    AuctionRequest auctionRequest;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    Account manager;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    Account staff;

}
