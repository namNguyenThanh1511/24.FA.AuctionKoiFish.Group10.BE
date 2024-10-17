package com.group10.koiauction.entity;

import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@Table(name = "auction_request")
public class AuctionRequest
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_request_id" , nullable = false)
    private Long auction_request_id;

    @Column(name = "title" , nullable = false)
    private String title;


    @NotNull(message = "Account creation date is required")
    @Past(message = "Creation date time must after current time")
    @Column(name = "created_date", nullable = false)
    private Date createdDate = new Date();

    @NotNull(message = "Account update date is required")
    @Past(message = "Update date time must after current time")
    @Column(name = "updated_date", nullable = false)
    private Date updatedDate = new Date();

    @Column(name = "description")
    private String description;

    @Column(name = "response_note")
    private String response_note;


    @NotNull(message = "Status can not be null")
    @Enumerated(EnumType.STRING)
    private AuctionRequestStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Account account;//breeder

    @ManyToOne // nhieu request -> 1 con ca koi
    @JoinColumn(name = "koi_id")//đặt tên cho khóa ngoại ở bảng AuctionRequest
    KoiFish koiFish;           // ánh xạ tới khóa chính trong bảng koi_fish

}
