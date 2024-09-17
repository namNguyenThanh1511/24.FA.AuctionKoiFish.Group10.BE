package com.group10.koiauction.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "koi_fish")
public class KoiFish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY for auto-increment
    @Column(name = "koi_id", nullable = false, updatable = false)
    private Long koi_id;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Breeder is required")
    @Column(name = "breeder", nullable = false)
    private String breeder;

    @Enumerated(EnumType.STRING) // For enum fields
    @Column(name = "sex", nullable = false)
    private Sex sex;

    @NotBlank(message = "Variety is required")
    @Column(name = "variety", nullable = false)
    private String variety;

    @NotNull(message = "Size in cm is required")
    @Column(name = "size_cm", nullable = false)
    private Double sizeCm;

    @NotNull(message = "Date of birth is required")
    @Column(name = "date_of_birth", nullable = false)
    private Date bornIn;

    @Lob // For large data like image URLs or binary data
    @Column(name = "image_url")
    private String image_url;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KoiStatus koiStatus;

    @Column(name = "estimated_value", nullable = false)
    private Double estimatedValue;


    // Enums
    public enum Sex {
        MALE, FEMALE , UNKNOWN
    }

    public enum KoiStatus {
        PENDING , REJECTED,PENDING_AUCTION , SELLING, ORDERED , SOLD,UNAVAILABLE,
        //chưa đc đưa vào phiên đấu giá :
        // - chờ duyệt ( PENDING) từ Staff và Manager -> koi_status = PENDING & request_status = PENDING
        //( khi Staff duyệt đơn -> request_status = PROCESSING ( CHỜ MANAGER DUYỆT LẦN CUỐI VÀ SET UP PHIÊN ĐẤU GIÁ ) & koi_status = PENDING
        //      Staff từ chối   -> koi_status = REJECTED & request_status = REJECTED
        // - Manager duyệt đơn và tạo phiên -> request_status = APPROVED
        //                                   & koi_status = PENDING_AUCTION
        //                                   & auction_status = ACTIVE
        //-  Manager từ chối     -> koi_status = REJECTED & request_status = REJECTED
        //-  KHI PHIÊN ĐANG DIỄN RA -> auction_status = ONGOING
        //                           & koi_status     = SELLING
        //-  KHI PHIÊN KẾT THÚC , TÌM RA NGƯỜI THẮNG -> auction_status : CLOSED
        //                                            & koi_status     : ORDERED ( đang trong order , chờ thanh toán )
        //                                            & order_status   : UNPAID
        //-  NGƯỜI THẮNG THANH TOÁN : order_status : PAID
        //-                         & koi_status   : SOLD
        //-  NGƯỜI THẮNG KO THANH TOÁN : order_status   : OVERDUE
        //                             & koi_status     : AUCTION_PENDING
        //                             & auction_status : ACTIVE
        // -LẶP LẠI QUÁ TRÌNH NHƯ LÚC PHIÊN ĐANG DIỄN RA
        //-  NÊU PHIÊN ĐANG DIỄN RA :  cá bị ảnh hưởng sức khỏe  -> koi_status = UNAVAILABLE
        //                                                        & auction_status = CANCELED
        // - NẾU CÁ ĐANG TRONG QUÁ TRÌNH BÀN GIAO ĐẾN KHÁCH HÀNG : cá bị ảnh hưởng sức khỏe
        //                                                       -> koi_status = UNAVAILABLE
        //                                                        & order_status = REFUNDING
        //

    }
}
