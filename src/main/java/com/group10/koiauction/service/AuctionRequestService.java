package com.group10.koiauction.service;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.KoiFish;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import com.group10.koiauction.exception.EntityNotFoundException;
import com.group10.koiauction.model.request.AuctionRequestDTO;
import com.group10.koiauction.model.request.AuctionRequestUpdateDTO;
import com.group10.koiauction.model.response.AuctionRequestResponse;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.AuctionRequestRepository;
import com.group10.koiauction.repository.KoiFishRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuctionRequestService {


    @Autowired
    private AuctionRequestRepository auctionRequestRepository;

    @Autowired
    private KoiFishRepository koiFishRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    @Lazy//delay the instantiation of a bean until it is required, which can break the circular dependency.
    private ModelMapper modelMapper;


    public AuctionRequestResponse createAuctionRequest(AuctionRequestDTO auctionRequestDTO) {
        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle(auctionRequestDTO.getTitle());
        auctionRequest.setDescription(auctionRequestDTO.getDescription());
        auctionRequest.setStatus(AuctionRequestStatusEnum.PENDING);
        auctionRequest.setAccount(getAccountById(auctionRequestDTO.getBreeder_id()));
        auctionRequest.setKoiFish(getKoiFishByID(auctionRequestDTO.getKoiFish_id(),true));
        updateKoiStatus(auctionRequestDTO.getKoiFish_id(),auctionRequest.getStatus());// ~pending
        try {
            auctionRequestRepository.save(auctionRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        AuctionRequestResponse auctionRequestResponse = new AuctionRequestResponse();
        auctionRequestResponse.setAuction_request_id(auctionRequest.getAuction_request_id());
        auctionRequestResponse.setCreatedDate(auctionRequest.getCreatedDate());
        auctionRequestResponse.setStatus(auctionRequest.getStatus());
        auctionRequestResponse.setTitle(auctionRequest.getTitle());
        auctionRequestResponse.setDescription(auctionRequest.getDescription());
        auctionRequestResponse.setBreeder_id(auctionRequestDTO.getBreeder_id());
        auctionRequestResponse.setKoi_id(auctionRequestDTO.getKoiFish_id());
        return auctionRequestResponse;
    }

    public AuctionRequestResponse updateAuctionRequest(Long id, AuctionRequestUpdateDTO auctionRequestDTO) {
        AuctionRequest auctionRequest = getAuctionRequestById(id);
        auctionRequest.setUpdatedDate(new Date());
        auctionRequest.setStatus(getAuctionRequestStatusEnum(auctionRequestDTO.getStatus()));// update status
        updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(),
                getAuctionRequestStatusEnum(auctionRequestDTO.getStatus()));
        auctionRequest.setAccount(auctionRequest.getAccount());
        try {
            auctionRequestRepository.save(auctionRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        AuctionRequestResponse auctionRequestResponse = new AuctionRequestResponse();
        auctionRequestResponse.setAuction_request_id(auctionRequest.getAuction_request_id());
        auctionRequestResponse.setCreatedDate(auctionRequest.getCreatedDate());
        auctionRequestResponse.setStatus(auctionRequest.getStatus());
        auctionRequestResponse.setTitle(auctionRequest.getTitle());
        auctionRequestResponse.setDescription(auctionRequest.getDescription());
        auctionRequestResponse.setBreeder_id(auctionRequest.getAccount().getUser_id());
        auctionRequestResponse.setKoi_id(auctionRequest.getKoiFish().getKoi_id());
        return auctionRequestResponse;

    }

    public Account getAccountById(Long id) {
        Account account = accountRepository.findByUser_id(id);
        if (account == null) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        } else if (account.getStatus() == AccountStatusEnum.INACTIVE) {
            throw new EntityNotFoundException("Account with id " + id + " is inactive");
        }
        return account;
    }

    public KoiFish getKoiFishByID(Long koi_id , boolean isCreate) {
        KoiFish koiFish = koiFishRepository.findByKoiId(koi_id);

        if (koiFish == null) {
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " not found");
        } else if (!koiFish.getKoiStatus().equals(KoiStatusEnum.AVAILABLE) && isCreate == true ) { // Chỉ đc lấy những
            // cá Koi
            // available để tạo AuctionRequest , nếu update == false => Có thể trả về tất cả cá koi ở tất cả status
            // trừ những cá koi ko tồn tại
            throw new EntityNotFoundException("KoiFish " + " with id : " + koi_id + " is not available");
        }
        return koiFish;
    }

    public AuctionRequest getAuctionRequestById(Long id) {
        AuctionRequest auctionRequest = auctionRequestRepository.findByAuctionRequestId(id);
        if (auctionRequest == null) {
            throw new EntityNotFoundException("AuctionRequest with id : " + id + " not found");
        }
        return auctionRequest;
    }

    public AuctionRequestStatusEnum getAuctionRequestStatusEnum(String status) {
        String statusX = status.toLowerCase().replaceAll("\\s", "");
        return switch (statusX) {
            case "pending" -> AuctionRequestStatusEnum.PENDING;
            case "acceptedbystaff" -> AuctionRequestStatusEnum.ACCEPTED_BY_STAFF;
            case "rejectedbystaff" -> AuctionRequestStatusEnum.REJECTED_BY_STAFF;
            case "approvedbymanager" -> AuctionRequestStatusEnum.APPROVED_BY_MANAGER;
            case "rejectedbymanager" -> AuctionRequestStatusEnum.REJECTED_BY_MANAGER;
            case "cancelled" -> AuctionRequestStatusEnum.CANCELLED;
            default -> throw new EntityNotFoundException("Invalid status");
        };
    }

    public void updateKoiStatus(Long id, AuctionRequestStatusEnum status) {
        KoiFish target = getKoiFishByID(id , false);
        switch (status) {
            case PENDING:{ // - chờ duyệt ( PENDING) từ Staff -> koi_status = PENDING & request_status = PENDING
                target.setKoiStatus(KoiStatusEnum.PENDING);
                target.setUpdatedDate(new Date());
                break;
            }
            case ACCEPTED_BY_STAFF:{ // Staff đã xác minh cá hợp lệ
                target.setKoiStatus(KoiStatusEnum.PENDING);//Chờ Manager duyệt
                target.setUpdatedDate(new Date());
                break;
            }
            case REJECTED_BY_STAFF:{// Staff đã xác minh cá ( estimate value , ngoại hình , sức khỏe ... ) ko hợp lệ , AuctionRequest bị reject
                target.setKoiStatus(KoiStatusEnum.UNAVAILABLE);
                target.setUpdatedDate(new Date());
                break;
            }case APPROVED_BY_MANAGER:{//Manager duyệt lần cuối thành công
                target.setKoiStatus(KoiStatusEnum.PENDING_AUCTION);// Cá chờ đc đưa lên đấu giá
                target.setUpdatedDate(new Date());
                break;
            }case REJECTED_BY_MANAGER:{//Manager thấy cá này ko có chiến lược mang lại lợi nhuận cho sàn , từ chối cá
                target.setKoiStatus(KoiStatusEnum.UNAVAILABLE);
                target.setUpdatedDate(new Date());
                break;
            }case CANCELLED:{//Khi Koi Breeder rút lại AuctionRequest vì lí do ...
                target.setKoiStatus(KoiStatusEnum.AVAILABLE); // Chuyển trạng thái cá lại từ "PENDING" -> "AVAILABLE"
                target.setUpdatedDate(new Date());
                break;
            }
        }
        try{
            koiFishRepository.save(target);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }


    }


}
