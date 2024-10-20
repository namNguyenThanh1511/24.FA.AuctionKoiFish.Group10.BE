package com.group10.koiauction.model.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NotNull
@AllArgsConstructor
@NoArgsConstructor
public class AuctionRequestDTO {
    private String title;
    private String description;
//    private Long breeder_id;
    private Long koiFish_id;
}
