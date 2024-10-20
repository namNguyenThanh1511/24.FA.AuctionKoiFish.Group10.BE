package com.group10.koiauction.api;


import com.group10.koiauction.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dataSource")
public class DataSourceAPI {

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping("")
    public ResponseEntity<String> createDataSource() {
        dataSourceService.createDataSource();
        return ResponseEntity.ok("Created a new DataSource");
    }

}
