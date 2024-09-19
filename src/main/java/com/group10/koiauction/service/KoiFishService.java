package com.group10.koiauction.service;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.entity.request.KoiFishRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.repository.VarietyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class KoiFishService {
    @Autowired
    KoiFishRepository koiFishRepository;
    @Autowired
    VarietyRepository varietyRepository;

    public KoiFish createKoiFish(KoiFishRequest koiFishRequest) {
        try {
            KoiFish koiFish = new KoiFish();
            Set<Variety> varieties = new HashSet<>();
            // check variety exist in data base

            for (Long id : koiFishRequest.getVarietiesID()) { // loop id in request koi fish ,
                Variety variety = varietyRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Variety Not Found with ID: " + id));
                varieties.add(variety);
            }
            koiFish.setName(koiFishRequest.getName());
            koiFish.setDescription(koiFishRequest.getDescription());
            koiFish.setBornIn(koiFishRequest.getBornIn());
            koiFish.setBreeder(koiFishRequest.getBreeder());
            koiFish.setImage_url(koiFishRequest.getImage_url());
            koiFish.setSex(koiFishRequest.getSex());
            koiFish.setSizeCm(koiFishRequest.getSizeCm());
            koiFish.setEstimatedValue(koiFishRequest.getEstimatedValue());
            koiFish.setVariety(koiFishRequest.getVariety());
            koiFish.setVarieties(varieties);
            return koiFishRepository.save(koiFish);

        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(e.getMessage()); // Rethrow or handle it as needed
        } catch (Exception e) {
            throw new DuplicatedEntity("Duplicated koi fish"); // Handle other exceptions separately
        }
    }


    public KoiFish updateKoiFish(Long koi_id, KoiFishRequest koiFishRequest) {
        KoiFish oldKoi = getKoiFishByID(koi_id);
        try {
            oldKoi.setName(koiFishRequest.getName());
            oldKoi.setDescription(koiFishRequest.getDescription());
            oldKoi.setBornIn(koiFishRequest.getBornIn());
            oldKoi.setBreeder(koiFishRequest.getBreeder());
            oldKoi.setImage_url(koiFishRequest.getImage_url());
            oldKoi.setSex(koiFishRequest.getSex());
            oldKoi.setSizeCm(koiFishRequest.getSizeCm());
            oldKoi.setEstimatedValue(koiFishRequest.getEstimatedValue());
            oldKoi.setVariety(koiFishRequest.getVariety());
            return koiFishRepository.save(oldKoi);
        } catch (Exception e) {
            throw new DuplicatedEntity("duplicated entity");
        }
    }

    public KoiFish deleteKoiFish(Long koi_id) {
        KoiFish target = getKoiFishByID(koi_id);
        target.setKoiStatus(KoiStatusEnum.UNAVAILABLE);
        return koiFishRepository.save(target);
    }

    public String deleteKoiFishDB(Long koi_id) {
        KoiFish target = getKoiFishByID(koi_id);
        koiFishRepository.delete(target);
        return "Deleted Successfully";
    }

    public KoiFish getKoiFishByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);

        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish not found");
        }
        return koiFish;
    }

}
