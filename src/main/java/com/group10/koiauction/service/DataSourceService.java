package com.group10.koiauction.service;

import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.mapper.KoiMapper;
import com.group10.koiauction.model.request.*;
import com.group10.koiauction.repository.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataSourceService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    KoiMapper koiMapper;

    @Autowired
    AuctionSessionMapper auctionSessionMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    KoiFishRepository koiFishRepository;

    @Autowired
    AuctionRequestRepository auctionRequestRepository;

    @Autowired
    AuctionSessionRepository auctionSessionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    KoiFishService koiFishService;

    @Autowired
    AuctionRequestService auctionRequestService;

    @Autowired
    AuctionSessionService auctionSessionService;

    @Autowired
    VarietyRepository varietyRepository;

    List<RegisterAccountRequest> registerAccountRequestList = new ArrayList<>();
    List<KoiFishRequest> koiFishRequestList = new ArrayList<>();
    List<VarietyRequest> varietyList = new ArrayList<>();
    List<AuctionRequestDTO> auctionRequestDTOList = new ArrayList<>();
    List<AuctionSessionRequestDTO> auctionSessionRequestDTOList = new ArrayList<>();


    private final List<String> KOI_IMAGE_URLS = List.of(
            "https://firebasestorage.googleapis.com/v0/b/swp391-koiauctionsystem.appspot.com/o/koihaku" +
                    ".jpg?alt=media&token=b2a1e1f3-aa0f-4f28-913e-52dd32aee8fe",
            "https://firebasestorage.googleapis.com/v0/b/swp391-koiauctionsystem.appspot.com/o/tancho" +
                    ".jpg?alt=media&token=e30628e2-a338-41c3-b13e-405d484e5a03",
            "https://firebasestorage.googleapis.com/v0/b/swp391-koiauctionsystem.appspot.com/o/Koi5" +
                    ".jpg?alt=media&token=0d8e609f-74ec-4ff5-a76d-b450b4031a1a",
            "https://firebasestorage.googleapis.com/v0/b/swp391-koiauctionsystem.appspot.com/o/w0726m001-v1019n011" +
                    ".jpg?alt=media&token=773cdadd-0346-4bd7-98e1-5284322b9737",
            "https://firebasestorage.googleapis.com/v0/b/swp391-koiauctionsystem.appspot.com/o/w0726m001-v1019n011.jpg?alt=media&token=425c8605-132b-430e-9c01-405b39d879f6"
    );


    public void createAccount(RegisterAccountRequest request) {
        try {
            Account account = new Account();
            account.setRoleEnum(authenticationService.getRoleEnumX(request.getRoleEnum()));
            request.setRoleEnum(authenticationService.getRoleEnumX(request.getRoleEnum()).toString());
            account = accountMapper.toAccount(request);
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            accountRepository.save(account);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public void createVariety(VarietyRequest request) {
        try {
            Variety variety = new Variety();
            variety.setName(request.getName());
            variety.setStatus(VarietyStatusEnum.ACTIVE);
            varietyRepository.save(variety);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createKoiFish(KoiFishRequest request) {
        try {
            KoiFish koiFish = new KoiFish();
            Set<Variety> varieties = koiFishService.getVarietiesByID(request.getVarietiesID());
            koiFish = koiMapper.toKoiFish(request);
            koiFish.setKoiStatus(KoiStatusEnum.AVAILABLE);
            koiFish.setVarieties(varieties);
            koiFish.setAccount(accountRepository.findAccountByUsername("koibreeder1"));
            koiFish = koiFishRepository.save(koiFish);
            System.out.println(koiFish);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createAuctionRequest(AuctionRequestDTO auctionRequestDTO) {
        AuctionRequest auctionRequest = new AuctionRequest();
        try {
            auctionRequest.setTitle(auctionRequestDTO.getTitle());
            auctionRequest.setDescription(auctionRequestDTO.getDescription());
            auctionRequest.setStatus(AuctionRequestStatusEnum.PENDING);
            auctionRequest.setAccount(accountRepository.findAccountByUsername("koibreeder1"));
            auctionRequest.setKoiFish(koiFishRepository.findByKoiId(auctionRequestDTO.getKoiFish_id()));
            auctionRequestRepository.save(auctionRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createAuctionSession(AuctionSessionRequestDTO auctionSessionRequestDTO) {
        try {
            AuctionSession auctionSession = auctionSessionMapper.toAuctionSession(auctionSessionRequestDTO);
            AuctionRequest auctionRequest = auctionRequestRepository.findByAuctionRequestId(auctionSessionRequestDTO.getAuction_request_id());
            auctionSession.setCurrentPrice(auctionSessionRequestDTO.getStartingPrice());
            auctionSession.setKoiFish(auctionRequest.getKoiFish());//lay ca tu auction request
            auctionSession.setAuctionRequest(auctionRequest);
            auctionSession.setStaff(accountRepository.findByUser_id(auctionSessionRequestDTO.getStaff_id()));
            auctionSession.setManager(accountRepository.findAccountByUsername("manager"));
            auctionSession.setStatus(AuctionSessionStatus.UPCOMING);
            auctionSession.setCreateAt(new Date());
            auctionSession.setUpdateAt(auctionSession.getCreateAt());
            auctionSessionRepository.save(auctionSession);
            auctionSessionService.updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(), auctionSession.getStatus());
            auctionRequestService.approveAuctionRequest(auctionRequest.getAuction_request_id());
            auctionSessionService.scheduleActivationJob(auctionSession);
        } catch (Exception e) {
            auctionRequestService.revertApproveAuctionRequest(auctionSessionRequestDTO.getAuction_request_id());
            throw new RuntimeException(e.getMessage() + " Trigger reverting ... ");
        }
    }

    public RegisterAccountRequest createRegisterAccountRequest(String username, String password, String firstName, String lastName, String email, String phoneNumber, String address, String roleEnum) {
        RegisterAccountRequest request = new RegisterAccountRequest(username, password, firstName, lastName, email, phoneNumber, address, roleEnum);
        registerAccountRequestList.add(request);
        return request;
    }

    public KoiFishRequest createKoiFishRequest(String name, KoiSexEnum sex, double sizeCm, double weightKg,
                                               Date bornIn, String image_url, String description,
                                               double estimatedValue, String video_url, Set<Long> varietiesID
    ) {
        KoiFishRequest request = new KoiFishRequest(name, sex, sizeCm, weightKg, bornIn, image_url, description,
                estimatedValue, video_url, varietiesID);
        koiFishRequestList.add(request);
        return request;
    }

    public VarietyRequest createVarietyRequest(String name) {
        VarietyRequest varietyRequest = new VarietyRequest(name);
        varietyList.add(varietyRequest);
        return varietyRequest;
    }

    public AuctionRequestDTO createAuctionRequestDTO(String title, String description, Long koiFishId) {
        AuctionRequestDTO request = new AuctionRequestDTO(title, description, koiFishId);
        auctionRequestDTOList.add(request);
        return request;
    }

    public AuctionSessionRequestDTO createAuctionSessionRequestDTO(String title,
                                                                   double startingPrice, double buyNowPrice,
                                                                   double bidIncrement, LocalDateTime startDate,
                                                                   LocalDateTime endDate,
                                                                   AuctionSessionType auctionType,
                                                                   double minBalanceToJoin, Long auctionRequestId,
                                                                   Long staffId) {
        AuctionSessionRequestDTO request = new AuctionSessionRequestDTO(title, startingPrice, buyNowPrice, bidIncrement, startDate, endDate, auctionType, minBalanceToJoin, auctionRequestId, staffId);
        auctionSessionRequestDTOList.add(request);
        return request;
    }

    public void createDataSource() {
        RegisterAccountRequest registerAccountRequest1 = createRegisterAccountRequest("manager", "11111111", "Nguyen", "Thanh Nam", "namntse170239@fpt.edu.vn", "0829017281", "ABC 123", "MANAGER");
        RegisterAccountRequest registerAccountRequest2 = createRegisterAccountRequest("staff", "11111111", "Makise", "Kurisu", "makise@gmail.com", "0829017282", "ABC 123", "STAFF");
        RegisterAccountRequest registerAccountRequest3 = createRegisterAccountRequest("koibreeder1", "11111111", "Le",
                "Thanh Hien", "hienlt@gmail.com", "0829017287", "ABC 123", "koibreeder");
        RegisterAccountRequest registerAccountRequest4 = createRegisterAccountRequest("member", "11111111", "Diep", "Thanh", "dipthanh@gmail.com", "0829017286", "ABC 123", "MEMBER");
        RegisterAccountRequest registerAccountRequest5 = createRegisterAccountRequest("member1", "11111111", "Test",
                "member", "member@gmail.com", "0829017285", "ABC 123", "MEMBER");
        //----------------------------------------------------------------------------------
        for (RegisterAccountRequest registerAccountRequest : registerAccountRequestList) {
            createAccount(registerAccountRequest);
        }
        //----------------------------------------------------------------------------------

        createVarietyRequest("Kohaku");
        createVarietyRequest("Sowa");
        createVarietyRequest("Ochibashigure");
        createVarietyRequest("Hirenaga");
        createVarietyRequest("Tancho");
        //----------------------------------------------------------------------------------
        for (VarietyRequest varietyRequest : varietyList) {
            createVariety(varietyRequest);
        }
        //----------------------------------------------------------------------------------

        createKoiFishRequest("Kohaku", KoiSexEnum.MALE, 50, 4.5, new Date(120, 5, 15), KOI_IMAGE_URLS.get(0),
                "Beautiful " +
                        "Kohaku with deep red pattern", 3000, "video_url1", Set.of(1L));
        createKoiFishRequest("Sanke", KoiSexEnum.FEMALE, 45, 3.8, new Date(121, 6, 10), KOI_IMAGE_URLS.get(1), "Well" +
                "-balanced " +
                "Sanke with crisp patterns", 2500, "video_url2", Set.of(2L));
        createKoiFishRequest("Showa", KoiSexEnum.MALE, 55, 5.0, new Date(119, 3, 5), KOI_IMAGE_URLS.get(2), "Bold " +
                "Showa with striking sumi patterns", 3500, "video_url3", Set.of(3L));
        createKoiFishRequest("Shiro Utsuri", KoiSexEnum.FEMALE, 48, 4.2, new Date(118, 8, 20), KOI_IMAGE_URLS.get(3),
                "Stunning Shiro Utsuri with pure white skin", 2800, "video_url4", Set.of(4L));
        createKoiFishRequest("Tancho", KoiSexEnum.MALE, 52, 4.9, new Date(117, 7, 25), KOI_IMAGE_URLS.get(4), "Unique" +
                " Tancho with a perfect red circle on the head", 4000, "video_url5", Set.of(5L));

        // Additional Koi Fish
        createKoiFishRequest("Asagi", KoiSexEnum.FEMALE, 60, 5.1, new Date(115, 4, 30), KOI_IMAGE_URLS.get(1), "Asagi with stunning netting pattern", 4500, "video_url6", Set.of(1L, 2L));
        createKoiFishRequest("Goshiki", KoiSexEnum.MALE, 58, 4.7, new Date(116, 3, 22), KOI_IMAGE_URLS.get(2), "Goshiki with vibrant contrast", 3800, "video_url7", Set.of(3L, 4L));
        createKoiFishRequest("Yamabuki Ogon", KoiSexEnum.FEMALE, 62, 5.3, new Date(117, 10, 14), KOI_IMAGE_URLS.get(3), "Bright Yamabuki Ogon with golden scales", 5000, "video_url8", Set.of(5L));

        //----------------------------------------------------------------------------------
        for (KoiFishRequest koiFishRequest : koiFishRequestList) {
            createKoiFish(koiFishRequest);
        }
        //----------------------------------------------------------------------------------
        createAuctionRequestDTO("Kohaku Auction", "Auction for a beautiful Kohaku",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url1").getKoi_id());
        createAuctionRequestDTO("Sanke Auction", "Auction for a well-balanced Sanke",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url2").getKoi_id());
        createAuctionRequestDTO("Asagi Auction", "Auction for a stunning Asagi",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url6").getKoi_id());
        createAuctionRequestDTO("Goshiki Auction", "Auction for a vibrant Goshiki",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url7").getKoi_id());
        createAuctionRequestDTO("Yamabuki Ogon Auction", "Auction for a bright Yamabuki Ogon",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url8").getKoi_id());

        //----------------------------------------------------------------------------------
        for (AuctionRequestDTO auctionRequestDTO : auctionRequestDTOList) {
            createAuctionRequest(auctionRequestDTO);
        }
        //----------------------------------------------------------------------------------
        createAuctionSessionRequestDTO(
                "Kohaku Special Auction",
                3000,
                5000,
                100,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusMinutes(10),
                AuctionSessionType.ASCENDING,
                500,
                auctionRequestRepository.findAuctionRequestByTitle("Kohaku Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());
        createAuctionSessionRequestDTO(
                "Sanke Exclusive Auction",
                2500,
                4500,
                150,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusMinutes(10),
                AuctionSessionType.ASCENDING,
                600,
                auctionRequestRepository.findAuctionRequestByTitle("Sanke Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());
        createAuctionSessionRequestDTO(
                "Asagi Auction",
                4500,
                7000,
                200,
                LocalDateTime.now().plusMinutes(15),
                LocalDateTime.now().plusMinutes(25),
                AuctionSessionType.ASCENDING,
                700,
                auctionRequestRepository.findAuctionRequestByTitle("Asagi Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());
        createAuctionSessionRequestDTO(
                "Goshiki Auction",
                3800,
                6000,
                180,
                LocalDateTime.now().plusMinutes(20),
                LocalDateTime.now().plusMinutes(30),
                AuctionSessionType.ASCENDING,
                650,
                auctionRequestRepository.findAuctionRequestByTitle("Goshiki Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());
        createAuctionSessionRequestDTO(
                "Yamabuki Ogon Auction",
                5000,
                8000,
                220,
                LocalDateTime.now().plusMinutes(25),
                LocalDateTime.now().plusMinutes(35),
                AuctionSessionType.ASCENDING,
                800,
                auctionRequestRepository.findAuctionRequestByTitle("Yamabuki Ogon Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());

        //----------------------------------------------------------------------------------
        for (AuctionSessionRequestDTO auctionSessionRequestDTO : auctionSessionRequestDTOList) {
            createAuctionSession(auctionSessionRequestDTO);
        }
        //----------------------------------------------------------------------------------
    }
}
