package com.group10.koiauction.service;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.model.response.KoiFishResponse;

import java.util.List;
import java.util.Optional;

public interface IKoiFishService {
    public List<KoiFish> getAll();

    public KoiFishResponse addKoiFish(KoiFish koiFish);

    public KoiFishResponse updateKoiFish(Long id,KoiFish koiFish );

    public KoiFishResponse deleteKoiFish(Long id);

    public Optional<KoiFishResponse> getKoiFish(Long id); // Optional : co the tra ve KoiFish hoac null



}
