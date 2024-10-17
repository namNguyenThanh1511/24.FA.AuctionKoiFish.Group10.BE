package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class AuctionSessionResponseKoiDTO {
    private Long id;
    private String name;
    private KoiSexEnum sex;
    private Double sizeCm;
    private Double weightKg;
    private Date bornIn;
    private String image_url;
    private String description;
    private Double estimatedValue;
    private KoiStatusEnum koiStatus;
    private String video_url;
    private BreederResponseDTO breeder;
    private Set<Variety> varieties ;
}
