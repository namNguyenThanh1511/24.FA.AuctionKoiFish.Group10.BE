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
import com.group10.koiauction.utilities.AccountUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private AccountUtils accountUtils;


    public AuctionRequestResponse createAuctionRequest(AuctionRequestDTO auctionRequestDTO) {
        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle(auctionRequestDTO.getTitle());
        auctionRequest.setDescription(auctionRequestDTO.getDescription());
        auctionRequest.setStatus(AuctionRequestStatusEnum.PENDING);
        auctionRequest.setAccount(accountUtils.getCurrentAccount());
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
        auctionRequestResponse.setBreeder_id(accountUtils.getCurrentAccount().getUser_id());
        auctionRequestResponse.setKoi_id(auctionRequestDTO.getKoiFish_id());

        return auctionRequestResponse;
    }

    public AuctionRequestResponse updateAuctionRequest(Long id, AuctionRequestUpdateDTO auctionRequestDTO) {
        AuctionRequest auctionRequest = getAuctionRequestById(id);
        auctionRequest.setUpdatedDate(new Date());
        auctionRequest.setStatus(getAuctionRequestStatusEnum(auctionRequestDTO.getStatus()));// update status
        updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(),auctionRequest.getStatus());
        auctionRequest.setAccount(auctionRequest.getAccount());
        auctionRequest.setResponse_note(auctionRequestDTO.getResponseNote());
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
        auctionRequestResponse.setResponseNote(auctionRequest.getResponse_note());
        return auctionRequestResponse;

    }

    public List<AuctionRequestResponse> getAllAuctionRequests(String status) {
        List<AuctionRequest> auctionRequestList;
        if(status.equals("")){
            auctionRequestList = auctionRequestRepository.findAll();
        }else{
            auctionRequestList = auctionRequestRepository.findByStatus(getAuctionRequestStatusEnum(status));
        }
        List<AuctionRequestResponse> auctionRequestResponseList = new ArrayList<>();
        for (AuctionRequest auctionRequest : auctionRequestList) {
            AuctionRequestResponse auctionRequestResponse = modelMapper.map(auctionRequest, AuctionRequestResponse.class);
            auctionRequestResponse.setKoi_id(auctionRequest.getKoiFish().getKoi_id());
            auctionRequestResponse.setBreeder_id(auctionRequest.getAccount().getUser_id());
            auctionRequestResponseList.add(auctionRequestResponse);
        }
        return auctionRequestResponseList;
    }
    public List<AuctionRequestResponse> getAllAuctionRequests(AuctionRequestStatusEnum status) {
        List<AuctionRequest> auctionRequests = auctionRequestRepository.findByStatus(status);
        List<AuctionRequestResponse> auctionRequestResponseList = new ArrayList<>();
        for (AuctionRequest auctionRequest : auctionRequests) {
            AuctionRequestResponse auctionRequestResponse = modelMapper.map(auctionRequest, AuctionRequestResponse.class);
            auctionRequestResponse.setKoi_id(auctionRequest.getKoiFish().getKoi_id());
            auctionRequestResponse.setBreeder_id(auctionRequest.getAccount().getUser_id());
            auctionRequestResponseList.add(auctionRequestResponse);
        }
        return auctionRequestResponseList;
    }

    public List<AuctionRequestResponse> getAllAuctionRequestsOfCurrentKoiBreeder() {
        List<AuctionRequestResponse> auctionRequestResponseList = new ArrayList<>();
        List<AuctionRequest> auctionRequests = auctionRequestRepository.findByBreederId(accountUtils.getCurrentAccount().getUser_id());
        for (AuctionRequest auctionRequest : auctionRequests) {
            AuctionRequestResponse auctionRequestResponse = new AuctionRequestResponse();
            auctionRequestResponse.setAuction_request_id(auctionRequest.getAuction_request_id());
            auctionRequestResponse.setTitle(auctionRequest.getTitle());
            auctionRequestResponse.setCreatedDate(auctionRequest.getCreatedDate());
            auctionRequestResponse.setDescription(auctionRequest.getDescription());
            auctionRequestResponse.setResponseNote(auctionRequest.getResponse_note());
            auctionRequestResponse.setStatus(auctionRequest.getStatus());
            auctionRequestResponse.setBreeder_id(auctionRequest.getAccount().getUser_id());
            auctionRequestResponse.setKoi_id(auctionRequest.getKoiFish().getKoi_id());
            auctionRequestResponseList.add(auctionRequestResponse);
        }
        return auctionRequestResponseList;
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
            // available để tạo AuctionRequest , nếu isCreate == true
            // => khi tạo auction request thì lấy những con koi AVAILABLE , còn khi update trạng thái của request
            // -> status cá koi cũng thay đổi theo -> đặt biến flag isCreate để mỗi khi tạo cá koi thì điều kiện này vẫn kich hoạt
            // còn khi update auction request -> có thể thay đổi status cá koi liên tục
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
                target.setKoiStatus(KoiStatusEnum.AVAILABLE);
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
