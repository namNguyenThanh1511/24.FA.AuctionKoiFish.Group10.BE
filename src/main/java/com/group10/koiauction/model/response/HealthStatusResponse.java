package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.KoiStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthStatusResponse {
    private Long koi_id;
    private KoiStatusEnum koiStatus;
    private String healthNote;
}
