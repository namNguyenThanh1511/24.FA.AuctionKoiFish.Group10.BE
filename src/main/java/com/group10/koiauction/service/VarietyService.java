package com.group10.koiauction.service;

import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import com.group10.koiauction.model.request.VarietyRequest;
import com.group10.koiauction.exception.DuplicatedEntity;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.repository.VarietyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class VarietyService {
    @Autowired
    VarietyRepository varietyRepository;

    public Variety addVariety(VarietyRequest varietyRequest) {
        try {
            Variety newVariety = new Variety();
            newVariety.setName(varietyRequest.getName());
            return varietyRepository.save(newVariety);
        }catch (Exception e){
            if(e.getMessage().contains("name")){
                throw new DuplicatedEntity("Duplicated name");
            }
        }
        return null;


    }
    public Set<Variety> getAllVarieties(){
        try {
            return varietyRepository.getAllVarieties();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Variety deleVariety(Long varietyId){
        Variety target = findVarietyById(varietyId);
        target.setStatus(VarietyStatusEnum.INACTIVE);
        return varietyRepository.save(target);
    }

    public Variety updateVariety(Long id, VarietyRequest varietyRequest) {
        Variety target = findVarietyById(id);
        target.setName(varietyRequest.getName());
        return varietyRepository.save(target);
    }

    public Variety findVarietyById(Long id) {
        Variety variety =
                varietyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Variety " + "with id : "+ id +
                        " Not " +
                        "Found"));
        return variety;
    }
}
