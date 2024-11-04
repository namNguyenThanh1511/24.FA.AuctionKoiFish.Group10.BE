package com.group10.koiauction.api;

import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.AuctionRequestProcess;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import com.group10.koiauction.model.request.AuctionRequestDTO;
import com.group10.koiauction.model.request.AuctionRequestUpdateDTO;
import com.group10.koiauction.model.request.ResponseAuctionRequestDTO;
import com.group10.koiauction.model.response.AcceptedAuctionRequestResponse;
import com.group10.koiauction.model.response.AuctionRequestProcessResponseDTO;
import com.group10.koiauction.model.response.AuctionRequestResponse;
import com.group10.koiauction.model.response.AuctionRequestResponsePagination;
import com.group10.koiauction.service.AuctionRequestProcessService;
import com.group10.koiauction.service.AuctionRequestService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctionRequest")
@CrossOrigin("*")
@SecurityRequirement(name = "api")// để sử dụng token tren swagger
public class AuctionRequestAPI {

    @Autowired
    AuctionRequestService auctionRequestService;

    @Autowired
    AuctionRequestProcessService auctionRequestProcessService;

    @PostMapping()
    @PreAuthorize("hasAuthority('KOI_BREEDER')")
    public ResponseEntity<AuctionRequestResponse> createAuctionRequest(@RequestBody AuctionRequestDTO auctionRequestDTO) {
        AuctionRequestResponse newAuctionRequest = auctionRequestService.createAuctionRequest(auctionRequestDTO);
        return ResponseEntity.ok(newAuctionRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionRequestResponse> updateAuctionRequest(@PathVariable Long id,
                                                                       @Valid @RequestBody AuctionRequestUpdateDTO auctionRequestDTO) {
        AuctionRequestResponse updatedAuctionRequest = auctionRequestService.updateAuctionRequest(id, auctionRequestDTO);
        return ResponseEntity.ok(updatedAuctionRequest);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AuctionRequestResponse>> getAllPendingAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests(
                "pending");
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @GetMapping("")
    public ResponseEntity<List<AuctionRequestResponse>> getAllAuctionRequests() {
        List<AuctionRequestResponse> auctionRequestResponseList = auctionRequestService.getAllAuctionRequests("");
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @GetMapping("/koiBreeder")
    public ResponseEntity<List<AuctionRequestResponse>> getAllAuctionRequestsForCurrentKoiBreeder() {
        List<AuctionRequestResponse> auctionRequestResponseList =
                auctionRequestService.getAllAuctionRequestsOfCurrentKoiBreeder();
        return ResponseEntity.ok(auctionRequestResponseList);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<AuctionRequestResponse> approveAuctionRequest(@PathVariable Long id,
                                                                        @RequestBody ResponseAuctionRequestDTO responseAuctionRequestDTO) {
        AuctionRequestResponse auctionRequestResponse =
                auctionRequestService.approveAuctionRequest(id, responseAuctionRequestDTO);
        return ResponseEntity.ok(auctionRequestResponse);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<AuctionRequestResponse> rejectAuctionRequest(@PathVariable Long id,
                                                                       @RequestBody ResponseAuctionRequestDTO responseAuctionRequestDTO) {
        AuctionRequestResponse auctionRequestResponse =
                auctionRequestService.rejectAuctionRequest(id, responseAuctionRequestDTO);
        return ResponseEntity.ok(auctionRequestResponse);
    }

    @PutMapping("/revertApprove/{id}")
    public ResponseEntity<String> revertApprovalAuctionRequest(@PathVariable Long id) {
        auctionRequestService.revertApproveAuctionRequest(id);
        return ResponseEntity.ok("Revert request successful");
    }

    @GetMapping("/processLog")
    public ResponseEntity<List<AuctionRequestProcessResponseDTO>> getAllProcessedAuctionRequests() {
        List<AuctionRequestProcessResponseDTO> auctionRequestProcessList = auctionRequestProcessService.getAllAuctionRequestProcess();
        return ResponseEntity.ok(auctionRequestProcessList);
    }

    @GetMapping("/koiBreeder/pagination")
    public ResponseEntity<AuctionRequestResponsePagination> getAllAuctionRequestOfCurrentBreederPagination(@RequestParam int page, @RequestParam int size) {
        AuctionRequestResponsePagination auctionRequestResponsePagination = auctionRequestService.getAuctionRequestResponsesPagination(page, size);
        return ResponseEntity.ok(auctionRequestResponsePagination);
    }
    @GetMapping("/koiBreeder/pagination/filter")
    public ResponseEntity<AuctionRequestResponsePagination> getAllAuctionRequestOfCurrentBreederPagination(
            @RequestParam(required = false) AuctionRequestStatusEnum status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam int page,
            @RequestParam int size) {
        Date startDateConverted = startDate != null ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date endDateConverted = endDate != null ? Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        AuctionRequestResponsePagination auctionRequestResponsePagination =
                auctionRequestService.getAuctionRequestResponsesPaginationOfCurrentBreederFilter(status, startDateConverted, endDateConverted, page, size);
        return ResponseEntity.ok(auctionRequestResponsePagination);
    }
    @GetMapping("/staff-only/pagination")
    public ResponseEntity<AuctionRequestResponsePagination> getAllAuctionRequestPaginationForStaff(@RequestParam int page, @RequestParam int size) {
        AuctionRequestResponsePagination auctionRequestResponsePagination = auctionRequestService.getAuctionRequestResponsesPaginationForStaff(page, size);
        return ResponseEntity.ok(auctionRequestResponsePagination);
    }

    @GetMapping("/staff-only/pagination/filter")
    public ResponseEntity<AuctionRequestResponsePagination> getAllAuctionRequestPaginationForStaff(@RequestParam int page, @RequestParam int size,
                                                                                                   @RequestParam(required = false) List<AuctionRequestStatusEnum> statusEnumList,
                                                                                                   @RequestParam(required = false) List<String> breederUsernameList) {
        AuctionRequestResponsePagination auctionRequestResponsePagination =
                auctionRequestService.getAuctionRequestResponsesPaginationForStaffWithFilter(page, size,
                        statusEnumList, breederUsernameList);
        return ResponseEntity.ok(auctionRequestResponsePagination);
    }


    @GetMapping("/manager-only/accepted-by-staff")
    public ResponseEntity<Map<String, Object>> getAuctionRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AcceptedAuctionRequestResponse> responses = auctionRequestService.getAcceptedByStaffAuctionRequests(page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("transactionResponseList", responses.getContent());
        response.put("pageNumber", responses.getNumber());
        response.put("pageSize", responses.getSize());
        response.put("totalPages", responses.getTotalPages());
        response.put("totalElements", responses.getTotalElements());
        return ResponseEntity.ok(response);
    }


}
