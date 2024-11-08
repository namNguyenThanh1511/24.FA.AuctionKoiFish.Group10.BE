package com.group10.koiauction.api;

import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.model.request.DeliveryStatusUpdateDTO;
import com.group10.koiauction.model.request.HealthStatusRequest;
import com.group10.koiauction.model.request.KoiFishRequest;
import com.group10.koiauction.model.response.HealthStatusResponse;
import com.group10.koiauction.model.response.KoiFishResponse;
import com.group10.koiauction.model.response.KoiFishResponsePagination;
import com.group10.koiauction.repository.KoiFishRepository;
import com.group10.koiauction.service.KoiFishService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/koiFish")
@CrossOrigin("*")
@SecurityRequirement(name="api")// để sử dụng token tren swagger
public class KoiFishAPI {
    @Autowired
    KoiFishService koiFishService;
    @Autowired
    KoiFishRepository koiFishRepository;
    @PostMapping()
    @PreAuthorize("hasAuthority('KOI_BREEDER')")
    public ResponseEntity<KoiFishResponse> createKoiFish(@Valid @RequestBody KoiFishRequest koiFishRequest) {
        KoiFishResponse koiFishResponse = koiFishService.createKoiFish(koiFishRequest);
        return ResponseEntity.ok(koiFishResponse);
    }
    @GetMapping("")
    public  ResponseEntity<List<KoiFishResponse>> getAllKoiFish (){
        List<KoiFishResponse> koiFishList = koiFishService.getAllKoiFish("");
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

    @GetMapping("/pagination")
    public ResponseEntity<KoiFishResponsePagination> getAllKoiFishPagination(@RequestParam int page,
                                                                             @RequestParam(defaultValue = "5") int size){
        KoiFishResponsePagination koiFishResponsePaginationList = koiFishService.getAllKoiFishPagination(page, size);
        return ResponseEntity.ok(koiFishResponsePaginationList);
    }

    @GetMapping("/koiBreeder/pagination")
    public ResponseEntity<KoiFishResponsePagination>getAllKoiFishOfCurrentBreederPagination(@RequestParam int page,
                                                                                            @RequestParam(defaultValue = "5") int size){
        KoiFishResponsePagination koiFishResponsePagination = koiFishService.getAllKoiFishOfCurrentBreederPagination(page, size);
        return ResponseEntity.ok(koiFishResponsePagination);
    }

    @GetMapping("/koiBreeder")
    public ResponseEntity<List<KoiFishResponse>>getAllKoiFishOfCurrentBreeder(){
        List<KoiFishResponse> koiFishResponseList = koiFishService.getAllKoiFishByCurrentBreeder("");
        return ResponseEntity.ok(koiFishResponseList);
    }

    @GetMapping("/koiBreeder/available")
    public ResponseEntity<List<KoiFishResponse>>getAllAvailableKoiFishOfCurrentBreeder(){
        List<KoiFishResponse> koiFishResponseList = koiFishService.getAllKoiFishByCurrentBreeder("AVAILABLE");
        return ResponseEntity.ok(koiFishResponseList);
    }

    @GetMapping("/koiBreeder/pagination/filter")
    public ResponseEntity<KoiFishResponsePagination> getAllKoiFishOfCurrentBreederFilter(@RequestParam(required = false)KoiStatusEnum status,
                                                                                     @RequestParam(required = false)KoiSexEnum sex,
                                                                                     @RequestParam(required = false)Double minSizeCm, @RequestParam(required = false)Double maxSizeCm,
                                                                                     @RequestParam(required = false)Double minWeightKg,@RequestParam(required = false)Double maxWeightKg,
                                                                                     @RequestParam(required = false)Double upperEstimatedValue,
                                                                                     @RequestParam(required = false)Double lowerEstimatedValue,
                                                                                     @RequestParam(required = false) Set<String> varietiesName,
                                                                                     @RequestParam int page,
                                                                                     @RequestParam int size){
       KoiFishResponsePagination koiFishResponseList =
               koiFishService.getAllKoiFishOfCurrentBreederPaginationWithFilter(status,sex,minSizeCm,maxSizeCm,
                       minWeightKg,maxWeightKg,upperEstimatedValue,lowerEstimatedValue,varietiesName,page,size);
        return ResponseEntity.ok(koiFishResponseList);
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

    @PutMapping("/health/{koi_id}")
    public ResponseEntity<HealthStatusResponse> updateKoiHealth(@PathVariable Long koi_id , @RequestBody HealthStatusRequest healthStatusRequest){
        HealthStatusResponse healthStatusResponse = koiFishService.updateHealthStatus(koi_id, healthStatusRequest);
        return ResponseEntity.ok(healthStatusResponse);
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<KoiFishResponse> markKoiFishAsReturned(@PathVariable Long id , @RequestBody DeliveryStatusUpdateDTO deliveryStatusUpdateDTO){
        KoiFishResponse koiFishResponse = koiFishService.markKoiFishAsReturned(id, deliveryStatusUpdateDTO);
        return ResponseEntity.ok(koiFishResponse);
    }



}
