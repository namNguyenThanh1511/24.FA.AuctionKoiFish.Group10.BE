package com.group10.koiauction.entity.enums;

public enum KoiStatusEnum {
    AVAILABLE,PENDING,PENDING_AUCTION , SELLING, WAITING_FOR_PAYMENT , SOLD,UNAVAILABLE,IS_DELETED
    // Chủ trang trại thêm cá vào hệ thống : koi_status = AVAILABLE

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
    //
    //-  KHI PHIÊN KẾT THÚC , KO AI ĐẤU GIÁ ->    auction_status : CLOSED
    //                                            & koi_status     :PENDING_AUCTION

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
