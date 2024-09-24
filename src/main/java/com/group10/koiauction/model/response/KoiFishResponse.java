package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

@Data
public class KoiFishResponse {
    private Long koi_id;
    private String name;
    private KoiSexEnum sex;
    private Double sizeCm;
    private Double weightKg;
    private Date bornIn;
    private String image_url;
    private String description;
    private Double estimatedValue;
    private KoiStatusEnum status;
    private Long breeder_id;
    private Set<Long> varietiesID;
}