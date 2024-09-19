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

    @NotBlank(message = "Breeder is required")
    @Column(name = "breeder", nullable = false)
    private String breeder;

    @Enumerated(EnumType.STRING) // For enum fields
    @Column(name = "sex", nullable = false)
    private KoiSexEnum sex;

    @NotBlank(message = "Variety is required")
    @Column(name = "variety", nullable = false)
    private String variety;

    @NotNull(message = "Size in cm is required")
    @Min(value = 1, message = "Size must be greater than 0")
    @Column(name = "size_cm", nullable = false)
    private Double sizeCm;

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
    private KoiStatusEnum koiStatus = KoiStatusEnum.PENDING;

    @NotNull(message = "Estimated value is required")
    @Min(value = 0, message = "Estimated value must be a positive number")
    @Column(name = "estimated_value", nullable = false)
    private Double estimatedValue;

    @ManyToMany
    @JoinTable(name = "koi_varieties", joinColumns = @JoinColumn(name = "koi_id"), inverseJoinColumns = @JoinColumn(name = "variety_id"))
    Set<Variety> varieties = new HashSet<>();

}
