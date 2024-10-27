package com.group10.koiauction.api;


import com.group10.koiauction.service.StatisticService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistic")
@CrossOrigin("*")
@SecurityRequirement(name="api")
public class StatisticAPI {

    @Autowired
    private StatisticService statisticService;

    @GetMapping
    public ResponseEntity getStatistic() {
        Map<String,Object> statisticMap = statisticService.getStatistic();
        return ResponseEntity.ok(statisticMap);
    }

}
