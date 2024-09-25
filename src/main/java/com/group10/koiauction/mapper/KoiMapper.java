package com.group10.koiauction.mapper;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.model.request.KoiFishRequest;
import com.group10.koiauction.model.response.KoiFishResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KoiMapper {

    KoiFish toKoiFish(KoiFishRequest koiFishRequest);
    @Mapping(source = "koiStatus", target = "koiStatus")
    KoiFishResponse toKoiFishResponse(KoiFish koiFish);
}
