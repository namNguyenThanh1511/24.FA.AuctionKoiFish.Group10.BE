package com.group10.koiauction.service;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.request.KoiFishRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.repository.KoiFishRepository;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KoiFishController {
    @Autowired
    KoiFishRepository koiFishRepository;

    public KoiFish createKoiFish(KoiFishRequest koiFishRequest) {
        try{
            KoiFish koiFish = new KoiFish();
            koiFish.setName(koiFishRequest.getName());
            koiFish.setDescription(koiFishRequest.getDescription());
            koiFish.setBornIn(koiFishRequest.getBornIn());
            koiFish.setBreeder(koiFishRequest.getBreeder());
            koiFish.setImage_url(koiFishRequest.getImage_url());
            koiFish.setSex(koiFishRequest.getSex());
            koiFish.setSizeCm(koiFishRequest.getSizeCm());
            koiFish.setEstimatedValue(koiFishRequest.getEstimatedValue());
            koiFish.setVariety(koiFishRequest.getVariety());
            return koiFishRepository.save(koiFish);

        }catch (Exception e){
            throw new DuplicatedEntity("duplicated entity");
        }

    }
}
