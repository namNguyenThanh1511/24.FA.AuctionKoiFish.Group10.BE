package com.group10.koiauction.mapper;


import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.model.response.AuctionSessionResponseAuctionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuctionRequestMapper {
    AuctionSessionResponseAuctionRequestDTO toAuctionSessionResponseAuctionRequestDTO(AuctionRequest auctionRequest);
}
