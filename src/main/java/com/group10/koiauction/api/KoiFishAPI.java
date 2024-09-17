package com.group10.koiauction.api;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.request.KoiFishRequest;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.service.KoiFishController;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koiFish")
public class KoiFishAPI {
    @Autowired
    KoiFishController koiFishController;
    @Autowired
    KoiFishRepository koiFishRepository;
    @PostMapping("create")
    public ResponseEntity<KoiFish> createKoiFish(@Valid @RequestBody KoiFishRequest koiFishRequest) {
        KoiFish koiFish = koiFishController.createKoiFish(koiFishRequest);
        return ResponseEntity.ok(koiFish);
    }
    @GetMapping("getAll")
    public  ResponseEntity<List<KoiFish>> getAllKoiFish (){
        List<KoiFish> koiFishList = koiFishRepository.findAll();
        return ResponseEntity.ok(koiFishList);
    }
    @GetMapping("/{koi_id}")
    public ResponseEntity<KoiFish> getKoiFish(@PathVariable Long koi_id){
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);
        return ResponseEntity.ok(koiFish);

    }

    @GetMapping("/getKoiByName/{name}")
    public ResponseEntity<List<KoiFish>> getKoiFishByKoiName(@PathVariable String name){
        List<KoiFish> koiFishList = koiFishRepository.findKoiFishByName(name);
        return ResponseEntity.ok(koiFishList);
    }

}