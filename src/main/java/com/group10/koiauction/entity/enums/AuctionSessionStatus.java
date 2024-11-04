package com.group10.koiauction.entity.enums;

public enum AuctionSessionStatus {
    UPCOMING,     // Phiên đấu giá sắp diễn ra (chưa bắt đầu).
    ONGOING,      // Phiên đấu giá đang diễn ra.
    COMPLETED,    // Phiên đấu giá đã kết thúc và có người thắng.
    CANCELLED,    // Phiên đấu giá đã bị hủy.
    NO_WINNER,    // Phiên đấu giá kết thúc nhưng không có người thắng (không ai đấu giá).
    DRAWN,        // Phiên đấu giá kết thúc với bốc thăm (trong trường hợp phương thức "Fixed Price").

}
