package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import com.group10.koiauction.model.request.KoiFishRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.response.KoiFishResponse;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.repository.VarietyRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KoiFishService {
    @Autowired
    KoiFishRepository koiFishRepository;
    @Autowired
    VarietyRepository varietyRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ModelMapper modelMapper;

    public KoiFishResponse createKoiFish(KoiFishRequest koiFishRequest) {
        KoiFish koiFish = modelMapper.map(koiFishRequest, KoiFish.class);
        try {
            Set<Variety> varieties = getVarietiesByID(koiFishRequest.getVarietiesID());
            koiFish.setKoiStatus(KoiStatusEnum.AVAILABLE);
            koiFish.setAccount(getAccountById(koiFishRequest.getBreeder_id()));
            koiFish.setVarieties(varieties);
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            koiFishRepository.save(koiFish);
            return koiFishResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()); // Handle other exceptions separately
        }
    }

    public List<KoiFishResponse> getAllKoiFish(String status) {
        List<KoiFish> koiFishList = koiFishRepository.findByKoiStatusEnum(getKoiStatusEnum(status));
        List<KoiFishResponse> koiFishResponseList = new ArrayList<>();
        for (KoiFish koiFish : koiFishList) {
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            koiFishResponseList.add(koiFishResponse);
        }
        return koiFishResponseList;
    }

    public KoiFishResponse updateKoiFish(Long koi_id, KoiFishRequest koiFishRequest) {
        KoiFish oldKoi = getKoiFishByID(koi_id);
        try {
            Set<Variety> varieties = getVarietiesByID(koiFishRequest.getVarietiesID());
            oldKoi.setName(koiFishRequest.getName());
            oldKoi.setSex(koiFishRequest.getSex());
            oldKoi.setSizeCm(koiFishRequest.getSizeCm());
            oldKoi.setWeightKg(koiFishRequest.getWeightKg());
            oldKoi.setBornIn(koiFishRequest.getBornIn());
            oldKoi.setImage_url(koiFishRequest.getImage_url());
            oldKoi.setDescription(koiFishRequest.getDescription());
            oldKoi.setEstimatedValue(koiFishRequest.getEstimatedValue());
            oldKoi.setUpdatedDate(new Date());
            oldKoi.setAccount(getAccountById(koiFishRequest.getBreeder_id()));
            oldKoi.setVarieties(varieties);
            koiFishRepository.save(oldKoi);
            KoiFishResponse koiFishResponse = getKoiMapperResponse(oldKoi);
            return koiFishResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public KoiFishResponse deleteKoiFish(Long koi_id) {
        KoiFish target = getKoiFishByID(koi_id);
        target.setUpdatedDate(new Date());
        target.setKoiStatus(KoiStatusEnum.UNAVAILABLE);
        koiFishRepository.save(target);
        return getKoiMapperResponse(target);
    }

    public String deleteKoiFishDB(Long koi_id) {
        KoiFish target = getKoiFishByID(koi_id);
        koiFishRepository.delete(target);
        return "Deleted Successfully";
    }

    public KoiFish getKoiFishByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);

        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        }
        return koiFish;
    }
    public KoiFishResponse getKoiFishResponseByID(Long koi_id) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);

        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        }
        return getKoiMapperResponse(koiFish);
    }

    public List<KoiFishResponse> getKoiFishListByName(String name) {
        List<KoiFish> koiFishList = koiFishRepository.findKoiFishByName(name);
        List<KoiFishResponse> koiFishResponseList = new ArrayList<>();
        for (KoiFish koiFish : koiFishList) {
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            koiFishResponseList.add(koiFishResponse);
        }
        return koiFishResponseList;
    }

    public Set<Variety> getVarietiesByID(Set<Long> varieties_id_request) {
        Set<Variety> varieties = new HashSet<>();
        for (Long id : varieties_id_request) { // loop id in request koi fish ,
            Variety variety = varietyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Variety Not Found with ID: " + id));
            if (variety.getStatus() == VarietyStatusEnum.ACTIVE) {
                varieties.add(variety);
            } else {
                throw new EntityNotFoundException("Variety Not active with ID: " + id);
            }

        }
        return varieties;
    }

    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }
        return account;
    }

    public KoiStatusEnum getKoiStatusEnum(String status) {
        String statusX = status.toLowerCase().replaceAll("\\s", "");
        return switch (statusX) {
            case "available" -> KoiStatusEnum.AVAILABLE;
            case "pending" -> KoiStatusEnum.PENDING;
            case "rejected" -> KoiStatusEnum.REJECTED;
            case "pendingauction" -> KoiStatusEnum.PENDING_AUCTION;
            case "selling" -> KoiStatusEnum.SELLING;
            case "ordered" -> KoiStatusEnum.ORDERED;
            case "sold" -> KoiStatusEnum.SOLD;
            case "unavailable" -> KoiStatusEnum.UNAVAILABLE;
            default -> throw new EntityNotFoundException("Invalid status");
        };
    }

    public Set<Long> getVarietiesIdOfKoi(KoiFish koiFish) {
        Set<Variety> varieties = koiFish.getVarieties();// lay ra ds varieties tu KoiFish
        Set<Long> varietiesIdSet = new HashSet<>();
        for (Variety variety : varieties) {
            varietiesIdSet.add(variety.getId());
        }
        return varietiesIdSet;
    }

    public KoiFishResponse getKoiMapperResponse(KoiFish koiFish) {
        KoiFishResponse koiFishResponse = modelMapper.map(koiFish, KoiFishResponse.class);
        koiFishResponse.setBreeder_id(koiFish.getAccount().getUser_id());
        koiFishResponse.setVarietiesID(getVarietiesIdOfKoi(koiFish));
        return koiFishResponse;
    }


}
