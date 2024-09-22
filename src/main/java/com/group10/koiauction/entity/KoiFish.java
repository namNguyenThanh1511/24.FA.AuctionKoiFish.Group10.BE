package com.group10.koiauction.entity;


import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    @Size(max = 100, message = "Name should not exceed 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING) // For enum fields
    @Column(name = "sex", nullable = false)
    private KoiSexEnum sex;


    @NotNull(message = "Size in cm is required")
    @Min(value = 1, message = "Size must be greater than 0")
    @Column(name = "size_cm", nullable = false)
    private Double sizeCm;

    @NotNull(message = "Weight in kg is required")
    @Min(value = 1, message = "Weight must be greater than 0")
    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be a past date")
    @Column(name = "date_of_birth", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bornIn;

    @Lob // For large data like image URLs or binary data
    @Column(name = "image_url")
    private String image_url;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KoiStatusEnum koiStatus;

    @NotNull(message = "Estimated value is required")
    @Min(value = 0, message = "Estimated value must be a positive number")
    @Column(name = "estimated_value", nullable = false)
    private Double estimatedValue;

    @NotNull(message = "Koi creation date is required")
    @Past(message = "Creation date time must after current time")
    private Date createdDate = new Date();

    @NotNull(message = "Koi update date is required")
    @Past(message = "Update date time must after current time")
    private Date updatedDate = new Date();

    @ManyToOne // 1 koi breeder co nhieu ca koi
    @JoinColumn(name = "user_id")
    Account account;


    @ManyToMany
    @JoinTable(name = "koi_varieties" // bảng trung gian của quan hệ nhiều-nhiều
            , joinColumns = @JoinColumn(name = "koi_id") // cột khóa chính cần nối với bảng variety
            , inverseJoinColumns = @JoinColumn(name = "variety_id"))  // cột khóa chính của bảng variety
            // 2 khóa chính nối vào bảng trung gian tạo thành 2 khóa ngoại ở bảng trung gian (koi_varieties)
    Set<Variety> varieties = new HashSet<>();

}
