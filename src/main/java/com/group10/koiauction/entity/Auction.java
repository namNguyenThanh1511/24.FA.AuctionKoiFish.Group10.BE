package com.group10.koiauction.entity;

import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.entity.enums.AuctionMethodEnum;
import com.group10.koiauction.entity.enums.AuctionStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class Auction {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;

    @NotNull
    private String title;

    @Min(value = 0)
    private String startingBid;

    @Min(value = 0)
    private String currentBid;

    @NotNull(message = "Starting date is required")
    @Past(message = "Start date time must after current time")
    private Date startDate = new Date();

    @NotNull(message = "End date is required")
    @Past(message = "End date time must after start time")
    private Date endDate = new Date();

    @NotNull
    @Min(value = 0, message = "Profit must be greater than 0, less than 100")
    @Max(value = 100, message = "Profit must be greater than 0, less than 100")
    private Float service_fee;

    @Enumerated(EnumType.STRING)
    private AuctionMethodEnum auctionMethod;

    @Enumerated(EnumType.STRING)
    private AuctionStatusEnum status = AuctionStatusEnum.PENDING;

    @ManyToOne
    @JoinColumn(name = "koi_id")
    private KoiFish koiFish;
}
