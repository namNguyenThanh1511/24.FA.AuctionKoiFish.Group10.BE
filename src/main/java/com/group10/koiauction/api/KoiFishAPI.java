package com.group10.koiauction.api;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.request.KoiFishRequest;
import com.group10.koiauction.service.KoiFishController;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/koiFish")
public class KoiFishAPI {
    @Autowired
    KoiFishController koiFishController;
    @PostMapping("create")
    public ResponseEntity<KoiFish> createKoiFish(@Valid @RequestBody KoiFishRequest koiFishRequest) {
        KoiFish koiFish = koiFishController.createKoiFish(koiFishRequest);
        return ResponseEntity.ok(koiFish);
    }
}
