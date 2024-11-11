package com.group10.koiauction.api;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.Variety;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.AuctionSessionType;
import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.model.request.AuctionSessionRequestDTO;
import com.group10.koiauction.model.request.DeliveryStatusUpdateDTO;
import com.group10.koiauction.model.request.UpdateStatusAuctionSessionRequestDTO;
import com.group10.koiauction.model.response.AuctionSessionResponsePagination;
import com.group10.koiauction.model.response.AuctionSessionResponsePrimaryDataDTO;
import com.group10.koiauction.model.response.KoiFishResponse;
import com.group10.koiauction.service.AuctionSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auctionSession")
@CrossOrigin("*")
@SecurityRequirement(name = "api")// để sử dụng token tren swagger
public class AuctionSessionAPI {

    @Autowired
    private AuctionSessionService auctionSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{id}")
    public ResponseEntity getAuctionSession(@PathVariable Long id) {
        AuctionSessionResponsePrimaryDataDTO response = auctionSessionService.getAuctionSessionResponsePrimaryDataDTO(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> createAuctionSession(@RequestBody AuctionSessionRequestDTO auctionSessionRequestDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponseDTO = auctionSessionService.createAuctionSession(auctionSessionRequestDTO);
        messagingTemplate.convertAndSend("/topic/auctionSession", "CREATED NEW AUCTION SESSION");
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

    @PutMapping("/close/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> closeAuctionSession(@PathVariable Long id) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionService.closeAuctionSession(id);
        return ResponseEntity.ok(auctionSessionResponsePrimaryDataDTO);
    }

    @GetMapping()
    public ResponseEntity<List<AuctionSessionResponsePrimaryDataDTO>> getAllAuctionSessions() {
        List<AuctionSessionResponsePrimaryDataDTO> auctionSessionResponseDTOs = auctionSessionService.getAllAuctionSessions();
        return ResponseEntity.ok(auctionSessionResponseDTOs);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> updateAuctionSessionStatus(@PathVariable Long id, @RequestBody UpdateStatusAuctionSessionRequestDTO updateStatusAuctionSessionRequestDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponseDTO = auctionSessionService.updateAuctionSessionStatus(id, updateStatusAuctionSessionRequestDTO);
        return ResponseEntity.ok(auctionSessionResponseDTO);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<AuctionSessionResponsePagination> getAuctionSessionsByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getAuctionSessionsByStaff(staffId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction-sessions-pagination")
    public ResponseEntity<AuctionSessionResponsePagination> getAllAuctionSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getAllAuctionSessionsPagination(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<AuctionSessionResponsePagination> searchAuctionSessions(
            @RequestParam(required = false) AuctionSessionType auctionType,
            @RequestParam(required = false) KoiSexEnum sex,
            @RequestParam(required = false) String breederName,
            @RequestParam(required = false) Set<String> varieties, // Change to Set<String>
            @RequestParam(required = false) Double minSizeCm,
            @RequestParam(required = false) Double maxSizeCm,
            @RequestParam(required = false) Double minWeightKg,
            @RequestParam(required = false) Double maxWeightKg,
            @RequestParam(required = false) AuctionSessionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AuctionSessionResponsePagination response = auctionSessionService.searchAuctionSessions(
                auctionType, sex, breederName, varieties, minSizeCm, maxSizeCm, minWeightKg, maxWeightKg, status, page, size);

        return ResponseEntity.ok(response); // Returns 200 OK status
    }

    @GetMapping("/my-auctions")
    public ResponseEntity<AuctionSessionResponsePagination> getAuctionSessionsForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getAuctionSessionsByCurrentUser(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-won-auctions")
    public ResponseEntity<AuctionSessionResponsePagination> getWonAuctionSessionsForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getWonAuctionSessionsByCurrentUser(page, size);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/end-session/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> processAuctionSession(@PathVariable Long id) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionService.processAuctionSessionById(id);
        return ResponseEntity.ok(auctionSessionResponsePrimaryDataDTO);
    }
    @GetMapping("/current-staff")
    public ResponseEntity<AuctionSessionResponsePagination> getAuctionSessionsByCurrentStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AuctionSessionResponsePagination response = auctionSessionService.getAuctionSessionsByCurrentStaff(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-staff/filter")
    public ResponseEntity<AuctionSessionResponsePagination> getFilteredAuctionSessionsByCurrentStaff(
            @RequestParam(required = false) AuctionSessionStatus auctionStatus,
            @RequestParam(required = false) AuctionSessionType auctionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AuctionSessionResponsePagination response = auctionSessionService.getFilteredAuctionSessionsByCurrentStaff(
                auctionStatus, auctionType, page, size);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/markDelivering/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> markDelivering(@PathVariable Long id , @RequestBody DeliveryStatusUpdateDTO deliveryStatusUpdateDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionService.markAuctionSessionAsDelivering(id, deliveryStatusUpdateDTO);
        return ResponseEntity.ok(auctionSessionResponsePrimaryDataDTO);
    }

    @PutMapping("/markDelivered/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> markDelivered(@PathVariable Long id , @RequestBody DeliveryStatusUpdateDTO deliveryStatusUpdateDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionService.markAuctionSessionAsDelivered(id, deliveryStatusUpdateDTO);
        return ResponseEntity.ok(auctionSessionResponsePrimaryDataDTO);
    }

    @PutMapping("/markDeliveryCancelled/{id}")
    public ResponseEntity<AuctionSessionResponsePrimaryDataDTO> markDeliveryCancelled(@PathVariable Long id , @RequestBody DeliveryStatusUpdateDTO deliveryStatusUpdateDTO) {
        AuctionSessionResponsePrimaryDataDTO auctionSessionResponsePrimaryDataDTO = auctionSessionService.markAuctionSessionAsCancelledDelivery(id, deliveryStatusUpdateDTO);
        return ResponseEntity.ok(auctionSessionResponsePrimaryDataDTO);
    }

    @PutMapping("/markKoiFishAsReturned/{id}")
    public ResponseEntity<KoiFishResponse> markKoiFishAsReturned(@PathVariable Long id , @RequestBody DeliveryStatusUpdateDTO deliveryStatusUpdateDTO){
        KoiFishResponse koiFishResponse = auctionSessionService.markKoiFishAsReturned(id, deliveryStatusUpdateDTO);
        return ResponseEntity.ok(koiFishResponse);
    }



}
