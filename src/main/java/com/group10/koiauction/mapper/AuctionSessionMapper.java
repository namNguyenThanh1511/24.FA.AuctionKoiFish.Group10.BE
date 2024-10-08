package com.group10.koiauction.mapper;

import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuctionSessionMapper {

    AuctionSession toAuctionSession(AuctionSessionRequestDTO auctionSessionRequestDTO);

    @Mapping(source = "status" , target = "auctionStatus")
    AuctionSessionResponseDTO toAuctionSessionResponseDTO(AuctionSession auctionSession);
}
