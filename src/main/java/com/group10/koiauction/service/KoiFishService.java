package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import com.group10.koiauction.mapper.KoiMapper;
import com.group10.koiauction.model.request.HealthStatusRequest;
import com.group10.koiauction.model.request.KoiFishRequest;

import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.response.HealthStatusResponse;
import com.group10.koiauction.model.response.KoiFishResponse;
import com.group10.koiauction.model.response.KoiFishResponsePagination;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.repository.VarietyRepository;

import com.group10.koiauction.utilities.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    KoiMapper koiMapper;

    @Autowired
    AccountUtils accountUtils;

    public KoiFishResponse createKoiFish(KoiFishRequest koiFishRequest) {
        KoiFish koiFish = koiMapper.toKoiFish(koiFishRequest);
        Set<Variety> varieties = getVarietiesByID(koiFishRequest.getVarietiesID());
        koiFish.setKoiStatus(KoiStatusEnum.AVAILABLE);
        koiFish.setAccount(accountUtils.getCurrentAccount());
        koiFish.setVarieties(varieties);

        try {
            koiFishRepository.save(koiFish);
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            return koiFishResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()); // Handle other exceptions separately
        }
    }

    public List<KoiFishResponse> getAllKoiFish(String status) {
        List<KoiFish> koiFishList = new ArrayList<>();
        if (status.equals("")) {
            koiFishList = koiFishRepository.findAll();
        } else {
            koiFishList = koiFishRepository.findByKoiStatusEnum(getKoiStatusEnum(status));
        }
        List<KoiFishResponse> koiFishResponseList = new ArrayList<>();
        for (KoiFish koiFish : koiFishList) {
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            koiFishResponseList.add(koiFishResponse);
        }
        return koiFishResponseList;
    }

    public KoiFishResponsePagination getAllKoiFishPagination(int page , int size) {

        Page<KoiFish> koiFishPage = koiFishRepository.findAll(PageRequest.of(page , size));
        List<KoiFishResponse> koiFishResponseList = new ArrayList<>();
        for (KoiFish koiFish : koiFishPage.getContent()) {
            KoiFishResponse koiFishResponse = getKoiMapperResponse(koiFish);
            koiFishResponseList.add(koiFishResponse);
        }
        KoiFishResponsePagination koiFishResponsePagination = new KoiFishResponsePagination();
        koiFishResponsePagination.setKoiFishResponseList(koiFishResponseList);
        koiFishResponsePagination.setPageNumber(koiFishPage.getNumber());
        koiFishResponsePagination.setTotalPages(koiFishPage.getTotalPages());
        koiFishResponsePagination.setTotalElements(koiFishPage.getNumberOfElements());
        return koiFishResponsePagination;
    }

    public List<KoiFishResponse> getAllKoiFishByCurrentBreeder(String status){
        List<KoiFish> koiFishList;
        if(status.equals("")){
            koiFishList = koiFishRepository.findKoiFishByBreeder(accountUtils.getCurrentAccount().getUser_id());
        }else{
            koiFishList = koiFishRepository.findKoiFishByBreederAndStatus(accountUtils.getCurrentAccount().getUser_id(), getKoiStatusEnum(status));
        }
        List<KoiFishResponse> koiFishResponseList = new ArrayList<>();
        for (KoiFish koiFish : koiFishList) {
            koiFishResponseList.add(getKoiMapperResponse(koiFish));
        }
        return koiFishResponseList;
    }

    public KoiFishResponse updateKoiFish(Long koi_id, KoiFishRequest koiFishRequest) {
        KoiFish oldKoi = getKoiFishByID(koi_id);
        try {
            Set<Variety> varieties = getVarietiesByID(koiFishRequest.getVarietiesID());//chuyen varieties id input -> varieties
            oldKoi.setName(koiFishRequest.getName());
            oldKoi.setSex(koiFishRequest.getSex());
            oldKoi.setSizeCm(koiFishRequest.getSizeCm());
            oldKoi.setWeightKg(koiFishRequest.getWeightKg());
            oldKoi.setBornIn(koiFishRequest.getBornIn());
            oldKoi.setImage_url(koiFishRequest.getImage_url());
            oldKoi.setDescription(koiFishRequest.getDescription());
            oldKoi.setEstimatedValue(koiFishRequest.getEstimatedValue());
            oldKoi.setUpdatedDate(new Date());
            oldKoi.setAccount(accountUtils.getCurrentAccount());
            oldKoi.setVarieties(varieties);
            koiFishRepository.save(oldKoi);
            KoiFishResponse koiFishResponse = getKoiMapperResponse(oldKoi);
            return koiFishResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public HealthStatusResponse updateHealthStatus(Long koi_id, HealthStatusRequest healthStatusRequest) {
        KoiFish oldKoi = getKoiFishByID(koi_id);
        oldKoi.setKoiStatus(getKoiStatusEnum(healthStatusRequest.getKoi_status()));
        oldKoi.setUpdatedDate(new Date());
        oldKoi.setHealth_note(healthStatusRequest.getHealthNote());
        try {
            koiFishRepository.save(oldKoi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HealthStatusResponse healthStatusResponse = new HealthStatusResponse();
        healthStatusResponse.setHealthNote(oldKoi.getHealth_note());
        healthStatusResponse.setKoiStatus(oldKoi.getKoiStatus());
        healthStatusResponse.setKoi_id(oldKoi.getKoi_id());
        return healthStatusResponse;
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
        try {
            koiFishRepository.delete(target);
        } catch (Exception e) {
            return e.getMessage();
        }

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
            if (variety.getStatus() == VarietyStatusEnum.ACTIVE) { //only get variety which is currently ACTIVE
                varietiesIdSet.add(variety.getId());
            }
        }
        return varietiesIdSet;
    }

    public KoiFishResponse getKoiMapperResponse(KoiFish koiFish) {
        KoiFishResponse koiFishResponse = koiMapper.toKoiFishResponse(koiFish);
        koiFishResponse.setBreeder_id(koiFish.getAccount().getUser_id());
        koiFishResponse.setVarietiesID(getVarietiesIdOfKoi(koiFish));// return varieties of KoiFish which is
        // currently ACTIVE
        koiFishResponse.setHealthNote(koiFish.getHealth_note());
        return koiFishResponse;
    }


}
