package com.group10.koiauction.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveWithDrawRequestDTO {
    private String responseNote;
    private String image_url;
}
