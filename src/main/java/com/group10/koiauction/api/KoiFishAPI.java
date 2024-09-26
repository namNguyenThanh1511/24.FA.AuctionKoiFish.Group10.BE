package com.group10.koiauction.api;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.model.request.KoiFishRequest;
import com.group10.koiauction.model.response.KoiFishResponse;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.service.KoiFishService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koiFish")
@CrossOrigin("*")
public class KoiFishAPI {
    @Autowired
    KoiFishService koiFishService;
    @Autowired
    KoiFishRepository koiFishRepository;
    @PostMapping()
    public ResponseEntity<KoiFishResponse> createKoiFish(@Valid @RequestBody KoiFishRequest koiFishRequest) {
        KoiFishResponse koiFishResponse = koiFishService.createKoiFish(koiFishRequest);
        return ResponseEntity.ok(koiFishResponse);
    }
    @GetMapping("/all/available")
    public  ResponseEntity<List<KoiFishResponse>> getAllKoiFish (){
        List<KoiFishResponse> koiFishList = koiFishService.getAllKoiFish("available");
        return ResponseEntity.ok(koiFishList);
    }
    @GetMapping("/{koi_id}")
    public ResponseEntity<KoiFishResponse> getKoiFish(@PathVariable Long koi_id){
        KoiFishResponse koiFish = koiFishService.getKoiFishResponseByID(koi_id);
        return ResponseEntity.ok(koiFish);

    }

    @GetMapping("/getKoiByName/{name}")
    public ResponseEntity<List<KoiFishResponse>> getKoiFishByKoiName(@PathVariable String name){
        List<KoiFishResponse> koiFishList = koiFishService.getKoiFishListByName(name);
        return ResponseEntity.ok(koiFishList);
    }
    @DeleteMapping("/{koi_id}")
    public ResponseEntity<KoiFishResponse> deleteKoiFish(@PathVariable Long koi_id){
        KoiFishResponse deleteKoi = koiFishService.deleteKoiFish(koi_id);
        return ResponseEntity.ok(deleteKoi);
    }
    @DeleteMapping("/deleteDB/{koi_id}")
    public ResponseEntity<String> deleteKoiFishDB(@PathVariable Long koi_id){
        String msg = koiFishService.deleteKoiFishDB(koi_id);
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/{koi_id}")
    public ResponseEntity<KoiFishResponse> updateKoiFish( @PathVariable Long koi_id,
                                            @Valid @RequestBody KoiFishRequest koiFishRequest) {
        KoiFishResponse updated_koi = koiFishService.updateKoiFish(koi_id, koiFishRequest);
        return ResponseEntity.ok(updated_koi);
    }


}
