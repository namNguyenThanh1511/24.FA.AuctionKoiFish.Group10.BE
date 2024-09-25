package com.group10.koiauction.api;

import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.VarietyStatusEnum;
import com.group10.koiauction.model.request.VarietyRequest;
import com.group10.koiauction.service.VarietyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/variety")
public class VarietyAPI {
    @Autowired
    private VarietyService varietyService;

    @PostMapping
    public ResponseEntity<Variety> createVariety(@Valid @RequestBody VarietyRequest varietyRequest) {
        Variety newVariety = varietyService.addVariety(varietyRequest);
        return ResponseEntity.ok(newVariety);
    }

    @GetMapping("/all")
    public ResponseEntity<Set<Variety>> getAllVarieties() {
        Set<Variety> varieties = varietyService.getAllVarieties(VarietyStatusEnum.ACTIVE);
        return ResponseEntity.ok(varieties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Variety> getVarietyById(@PathVariable Long id) {
        Variety variety    =  varietyService.findVarietyById(id);
        return ResponseEntity.ok(variety);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Variety> updateVariety(@PathVariable Long id, @Valid @RequestBody VarietyRequest varietyRequest) {
        Variety updatedVariety = varietyService.updateVariety(id, varietyRequest);
        return ResponseEntity.ok(updatedVariety);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Variety> deleteVariety(@PathVariable Long id) {
        Variety updatedVariety = varietyService.deleteVariety(id);
        return ResponseEntity.ok(updatedVariety);
    }

    @PutMapping("/status/activate/{id}")
    public ResponseEntity<Variety> activateVariety(@PathVariable Long id) {
        Variety updatedVariety = varietyService.activateVariety(id);
        return ResponseEntity.ok(updatedVariety);
    }


}
