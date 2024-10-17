package com.group10.koiauction.mapper;

import com.group10.koiauction.entity.Bid;
import com.group10.koiauction.model.response.BidResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BidMapper {
    BidResponseDTO toBidResponseDTO(Bid bid);
}
