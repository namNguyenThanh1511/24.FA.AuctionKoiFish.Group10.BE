package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class AcceptedAuctionRequestResponse {
    private Long id;
    private Date createdAt;
    private String description;
    private AuctionRequestStatusEnum status;
    private BreederResponseDTO breeder;
    private Long koiId;
<<<<<<< HEAD
}
=======
}
>>>>>>> bb6f36f00b2310d3d003f3c5df89f41fc1ac6a49
