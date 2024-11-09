package com.group10.koiauction.service;

import com.group10.koiauction.entity.*;
import com.group10.koiauction.entity.enums.*;
import com.group10.koiauction.exception.BidException;
import com.group10.koiauction.mapper.AccountMapper;
import com.group10.koiauction.mapper.AuctionSessionMapper;
import com.group10.koiauction.mapper.BidMapper;
import com.group10.koiauction.mapper.KoiMapper;
import com.group10.koiauction.model.datasource.DsAuctionRequestDTO;
import com.group10.koiauction.model.datasource.DsBidRequestDTO;
import com.group10.koiauction.model.request.*;
import com.group10.koiauction.model.response.AuctionSessionResponseAccountDTO;
import com.group10.koiauction.model.response.BidResponseDTO;
import com.group10.koiauction.repository.*;
import com.group10.koiauction.utilities.AccountUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    BidService bidService;

    @Autowired
    VarietyRepository varietyRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    BidMapper bidMapper;

    List<RegisterAccountRequest> registerAccountRequestList = new ArrayList<>();
    List<KoiFishRequest> koiFishRequestList = new ArrayList<>();
    List<VarietyRequest> varietyList = new ArrayList<>();
    List<DsAuctionRequestDTO> dsAuctionRequestDTOList = new ArrayList<>();
    List<AuctionSessionRequestDTO> auctionSessionRequestDTOList = new ArrayList<>();
    List<DsBidRequestDTO> dsBidRequestDTOList = new ArrayList<>();


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
    @Autowired
    private AccountUtils accountUtils;


    public void createAccount(RegisterAccountRequest request) {
        try {
            Account account = new Account();
            account.setRoleEnum(authenticationService.getRoleEnumX(request.getRoleEnum()));
            request.setRoleEnum(authenticationService.getRoleEnumX(request.getRoleEnum()).toString());
            account = accountMapper.toAccount(request);
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setBalance(100000);
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

    public void createAuctionRequest(DsAuctionRequestDTO dsAuctionRequestDTO) {
        AuctionRequest auctionRequest = new AuctionRequest();
        try {
            auctionRequest.setCreatedDate(dsAuctionRequestDTO.getCreatedDate());
            auctionRequest.setTitle(dsAuctionRequestDTO.getTitle());
            auctionRequest.setDescription(dsAuctionRequestDTO.getDescription());
            auctionRequest.setStatus(AuctionRequestStatusEnum.PENDING);
            auctionRequest.setAccount(accountRepository.findAccountByUsername("koibreeder1"));
            auctionRequest.setKoiFish(koiFishRepository.findByKoiId(dsAuctionRequestDTO.getKoiFish_id()));
            auctionRequestRepository.save(auctionRequest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createAuctionSession(AuctionSessionRequestDTO auctionSessionRequestDTO, boolean isParticipated) {
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
            auctionSession = auctionSessionRepository.save(auctionSession);
            auctionSessionService.updateKoiStatus(auctionRequest.getKoiFish().getKoi_id(), auctionSession.getStatus());
//            auctionRequestService.approveAuctionRequest(auctionRequest.getAuction_request_id());
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

    public DsAuctionRequestDTO createAuctionRequestDTO(String title, String description, Long koiFishId, Date createdDate) {
        DsAuctionRequestDTO request = new DsAuctionRequestDTO(title, description, koiFishId, createdDate);
        dsAuctionRequestDTOList.add(request);
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

    public DsBidRequestDTO createDsBidRequestDTO(double amount, Long auctionSessionId, Long memberId) {
        DsBidRequestDTO request = new DsBidRequestDTO(amount, auctionSessionId, memberId);
        dsBidRequestDTOList.add(request);
        return request;
    }

    public Bid createBid(DsBidRequestDTO bidRequestDTO) {
        AuctionSession auctionSession =
                auctionSessionRepository.findAuctionSessionById(bidRequestDTO.getAuctionSessionId());
        Account memberAccount = accountRepository.findByUser_id(bidRequestDTO.getMemberId());
        double bidRequestAmountIncrement = bidRequestDTO.getBidAmount();
        double previousMaxBidAmount;

        if (auctionSession.getBidSet() == null) {
            previousMaxBidAmount = 0;
        } else {
            previousMaxBidAmount = bidService.findMaxBidAmount(auctionSession.getBidSet());// ko co ai dau gia ->
        }
        // lay gia
        // hien tai
        if (previousMaxBidAmount == 0) {
            previousMaxBidAmount = auctionSession.getCurrentPrice();
        }
        double currentBidAmount = previousMaxBidAmount + bidRequestAmountIncrement;
        //Lấy bid gần nhất của member hiện tại (người đang đấu giá ) để tính lượng tiêu hao trong ví
        Bid latestBidOfCurrentMember =
                bidRepository.getLatestBidAmountOfCurrentMemberOfAuctionSession(memberAccount.getUser_id(),
                        auctionSession.getAuctionSessionId());
        double memberLostAmount;
        if (latestBidOfCurrentMember != null) {// nếu member đã có đấu giá trong phiên này
            memberLostAmount = currentBidAmount - latestBidOfCurrentMember.getBidAmount();
        } else {// lần đầu tiên member  đấu giá trong phiên này
            memberLostAmount = auctionSession.getCurrentPrice() + bidRequestAmountIncrement;
        }
        if (memberAccount.getBalance() < auctionSession.getMinBalanceToJoin()) {
            throw new BidException("Your balance does not have enough money to join the auction." + "You currently " +
                    "have  " + memberAccount.getBalance() + " but required : " + auctionSession.getMinBalanceToJoin());
        }
        if (bidRequestDTO.getBidAmount() > memberAccount.getBalance()) {
            throw new BidException("Your account does not have enough money to bid this amount ");
        }
        if (currentBidAmount < previousMaxBidAmount + auctionSession.getBidIncrement()) {
            throw new BidException("Your bid is lower than the required minimum increment.");
        }

        if (memberLostAmount > memberAccount.getBalance()) {
            throw new BidException("Your account does not have enough money to bid ! " + "You currently have : " + memberAccount.getBalance() + " But required " + memberLostAmount);
        }

        if (currentBidAmount >= auctionSession.getBuyNowPrice()) {//khi đấu giá vượt quá Buy Now ->
            // chuyển sang buy now , ko tính là bid nữa
            throw new RuntimeException("You can buy now this fish");
        }
        Set<Bid> bidSet = auctionSession.getBidSet();
        if (bidSet == null) {
            bidSet = new HashSet<>();
        }
        Bid bid = new Bid();
        bid.setBidAt(new Date());
        bid.setBidAmount(currentBidAmount);
        bid.setAuctionSession(auctionSession);
        bid.setMember(memberAccount);
        bidService.updateAuctionSessionCurrentPrice(currentBidAmount, auctionSession);
        bidSet.add(bid);

        Transaction transaction = new Transaction();
        transaction.setCreateAt(new Date());
        transaction.setFrom(memberAccount);
        transaction.setType(TransactionEnum.BID);
        transaction.setAmount(memberLostAmount);
        transaction.setDescription("Bidding (-)  " + memberLostAmount);
        transaction.setStatus(TransactionStatus.SUCCESS);

        transaction.setBid(bid);
        auctionSession.setBidSet(bidSet);
        transaction.setAuctionSession(auctionSession);
        bid.setTransaction(transaction);
        memberAccount.setBalance(memberAccount.getBalance() - memberLostAmount);

        try {
            bid = bidRepository.save(bid);
            transactionRepository.save(transaction);
            auctionSessionRepository.save(auctionSession);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }

        return bid;
    }


    public void createDataSource() {
        createAccountsCollections();

        createVarietyCollections();

        createKoiFishCollections();

        createAuctionRequestCollections();

        createAuctionSessionCollections();

        auctionSessionRequestDTOList.clear();

        createCompletedAuctionSessionCollections();

        dsBidRequestDTOList.clear();
        customBid();

    }

    public void createAccountsCollections() {
        RegisterAccountRequest registerAccountRequest1 = createRegisterAccountRequest("manager", "11111111", "Nguyen", "Thanh Nam", "namntse170239@fpt.edu.vn", "0829017281", "ABC 123", "MANAGER");
        RegisterAccountRequest registerAccountRequest2 = createRegisterAccountRequest("staff", "11111111", "Makise", "Kurisu", "makise@gmail.com", "0829017282", "ABC 123", "STAFF");
        RegisterAccountRequest registerAccountRequest3 = createRegisterAccountRequest("koibreeder1", "11111111", "Le",
                "Thanh Hien", "hienlt@gmail.com", "0829017287", "ABC 123", "koibreeder");
        RegisterAccountRequest registerAccountRequest4 = createRegisterAccountRequest("member", "11111111", "Diep", "Thanh", "dipthanh@gmail.com", "0829017286", "ABC 123", "MEMBER");
        RegisterAccountRequest registerAccountRequest5 = createRegisterAccountRequest("member1", "11111111", "Test",
                "member", "member@gmail.com", "0829017285", "ABC 123", "MEMBER");
        RegisterAccountRequest breederAccount1 = createRegisterAccountRequest("NND", "11111111", "Nguyen", "NND", "nnd@gmail.com", "0829017301", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount2 = createRegisterAccountRequest("Marushin", "11111111", "Maru", "Shin", "marushin@gmail.com", "0829017302", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount3 = createRegisterAccountRequest("Sakai", "11111111", "Saka", "I", "sakai@gmail.com", "0829017303", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount4 = createRegisterAccountRequest("Isa", "11111111", "Isa", "Koi", "isa@gmail.com", "0829017304", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount5 = createRegisterAccountRequest("Maruhiro", "11111111", "Maru", "Hiro", "maruhiro@gmail.com", "0829017305", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount6 = createRegisterAccountRequest("Torazo", "11111111", "Tora", "Zo", "torazo@gmail.com", "0829017306", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount7 = createRegisterAccountRequest("Shinoda Kanno", "11111111", "Shinoda", "Kanno", "shinoda.kanno@gmail.com", "0829017307", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount8 = createRegisterAccountRequest("Dainichi", "11111111", "Dai", "Nichi", "dainichi@gmail.com", "0829017308", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount9 = createRegisterAccountRequest("Omosako", "11111111", "Omo", "Sako", "omosako@gmail.com", "0829017309", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount10 = createRegisterAccountRequest("Izumiya", "11111111", "Izu", "Miya", "izumiya@gmail.com", "0829017310", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount11 = createRegisterAccountRequest("Marudo", "11111111", "Maru", "Do", "marudo@gmail.com", "0829017311", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount12 = createRegisterAccountRequest("Marujyu", "11111111", "Maru", "Jyu", "marujyu@gmail.com", "0829017312", "ABC 123", "koibreeder");
        RegisterAccountRequest breederAccount13 = createRegisterAccountRequest("Shintaro", "11111111", "Shin", "Taro", "shintaro@gmail.com", "0829017313", "ABC 123", "koibreeder");
        createRegisterAccountRequest("member2", "11111111", "Alice", "Johnson", "alice.johnson@gmail.com", "0829017283", "XYZ 456", "MEMBER");
        createRegisterAccountRequest("member3", "11111111", "Bob", "Smith", "bob.smith@gmail.com", "0829017284", "XYZ 456", "MEMBER");
        createRegisterAccountRequest("member4", "11111111", "Carol", "Williams", "carol.williams@gmail.com", "0829017289", "XYZ 456", "MEMBER");
        //----------------------------------------------------------------------------------
        for (RegisterAccountRequest registerAccountRequest : registerAccountRequestList) {
            createAccount(registerAccountRequest);
        }
    }

    public void createVarietyCollections() {
        createVarietyRequest("Kohaku");
        createVarietyRequest("Showa");
        createVarietyRequest("Ochibashigure");
        createVarietyRequest("Hirenaga");
        createVarietyRequest("Tancho");
        createVarietyRequest("Kiryuu");
        //----------------------------------------------------------------------------------
        for (VarietyRequest varietyRequest : varietyList) {
            createVariety(varietyRequest);
        }
    }

    public void createKoiFishCollections() {
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
        createKoiFishRequest("Tancho", KoiSexEnum.MALE, 52, 4.9, new Date(117, 7, 25), KOI_IMAGE_URLS.get(0), "Unique" +
                " Tancho with a perfect red circle on the head", 4000, "video_url5", Set.of(5L));

        // Additional Koi Fish
        createKoiFishRequest("Asagi", KoiSexEnum.FEMALE, 60, 5.1, new Date(115, 4, 30), KOI_IMAGE_URLS.get(1), "Asagi with stunning netting pattern", 4500, "video_url6", Set.of(1L, 2L));
        createKoiFishRequest("Goshiki", KoiSexEnum.MALE, 58, 4.7, new Date(116, 3, 22), KOI_IMAGE_URLS.get(2), "Goshiki with vibrant contrast", 3800, "video_url7", Set.of(3L, 4L));
        createKoiFishRequest("Yamabuki Ogon", KoiSexEnum.FEMALE, 62, 5.3, new Date(117, 10, 14), KOI_IMAGE_URLS.get(3), "Bright Yamabuki Ogon with golden scales", 5000, "video_url8", Set.of(5L));

        createKoiFishRequest("Kikokuryu", KoiSexEnum.FEMALE, 61, 4.9, new Date(116, 2, 15), KOI_IMAGE_URLS.get(0),
                "Kikokuryu with striking black and silver pattern", 4600, "video_url9", Set.of(1L, 2L));
        createKoiFishRequest("Hi Utsuri", KoiSexEnum.MALE, 53, 4.3, new Date(118, 9, 17), KOI_IMAGE_URLS.get(1), "Hi " +
                "Utsuri with vibrant red and black", 3200, "video_url10", Set.of(5L));
        createKoiFishRequest("Shusui", KoiSexEnum.FEMALE, 57, 5.0, new Date(117, 12, 5), KOI_IMAGE_URLS.get(2),
                "Shusui with beautiful blue and red scale pattern", 3700, "video_url11", Set.of(4L));

        //----------------------------------------------------------------------------------
        for (KoiFishRequest koiFishRequest : koiFishRequestList) {
            createKoiFish(koiFishRequest);
        }
    }

    public void createAuctionRequestCollections() {
        createAuctionRequestDTO("Kohaku Auction", "Auction for a beautiful Kohaku",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url1").getKoi_id(), new Date());
        createAuctionRequestDTO("Sanke Auction", "Auction for a well-balanced Sanke",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url2").getKoi_id(), new Date());
        createAuctionRequestDTO("Asagi Auction", "Auction for a stunning Asagi",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url6").getKoi_id(), new Date());
        createAuctionRequestDTO("Goshiki Auction", "Auction for a vibrant Goshiki",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url7").getKoi_id(), new Date());
        createAuctionRequestDTO("Yamabuki Ogon Auction", "Auction for a bright Yamabuki Ogon",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url8").getKoi_id(), new Date());
        createAuctionRequestDTO("Kikokuryu Auction", "Auction for a striking Kikokuryu",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url9").getKoi_id(), getPastDate(4));
        createAuctionRequestDTO("Hi Utsuri Auction", "Auction for a vibrant Hi Utsuri",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url10").getKoi_id(), getPastDate(4));
        createAuctionRequestDTO("Shusui Auction", "Auction for a beautiful Shusui",
                koiFishRepository.findExactKoiFishByVideoUrl("video_url11").getKoi_id(), getPastDate(5));


        //----------------------------------------------------------------------------------
        for (DsAuctionRequestDTO dsAuctionRequestDTO : dsAuctionRequestDTOList) {
            createAuctionRequest(dsAuctionRequestDTO);
        }
    }

    public void createAuctionSessionCollections() {
        String[] auctionRequestTitles = {
                "Kohaku Auction",
                "Sanke Auction",
                "Asagi Auction",
                "Goshiki Auction",
                "Yamabuki Ogon Auction",
                "Kikokuryu Auction",
                "Hi Utsuri Auction",
                "Shusui Auction"
        };

        for (String title : auctionRequestTitles) {
            AuctionRequest auctionRequest = auctionRequestRepository.findAuctionRequestByTitle(title);
            Date createdDate = auctionRequest.getCreatedDate();
            LocalDate createdLocalDate = createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            long daysInThePast = ChronoUnit.DAYS.between(createdLocalDate, LocalDate.now());
            Long auctionRequestId = auctionRequestRepository.findAuctionRequestByTitle(title).getAuction_request_id();

            auctionRequestService.approveAuctionRequest(auctionRequestId, accountRepository.findAccountByUsername(
                    "staff"), getPastDate((int) daysInThePast - 1));
            auctionRequestService.approveAuctionRequest(auctionRequestId, accountRepository.findAccountByUsername(
                    "manager"), getPastDate((int)daysInThePast - 2));
        }

        createAuctionSessionRequestDTO(
                "Kohaku Special Auction",
                2000,
                5000,
                0,
                LocalDateTime.now().plusMinutes(2),
                LocalDateTime.now().plusMinutes(5),
                AuctionSessionType.FIXED_PRICE,
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
            createAuctionSession(auctionSessionRequestDTO, false);
        }
    }

    public void createCompletedAuctionSessionCollections() {
        createAuctionSessionRequestDTO(
                "Kikokuryu Exclusive Auction",
                4600,
                7000,
                100,
                LocalDateTime.now().minusDays(1),    // Starts 1 day ago
                LocalDateTime.now().minusHours(20),  // Ends 20 hours ago
                AuctionSessionType.ASCENDING,
                600,
                auctionRequestRepository.findAuctionRequestByTitle("Kikokuryu Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());

        createAuctionSessionRequestDTO(
                "Hi Utsuri Special Auction",
                3200,
                5500,
                100,
                LocalDateTime.now().minusDays(2),     // Starts 2 days ago
                LocalDateTime.now().minusDays(1).plusHours(12), // Ends 1.5 days ago
                AuctionSessionType.ASCENDING,
                550,
                auctionRequestRepository.findAuctionRequestByTitle("Hi Utsuri Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());

        createAuctionSessionRequestDTO(
                "Shusui Auction Event",
                3700,
                6200,
                100,
                LocalDateTime.now().minusHours(2),    // Starts 2 hours ago
                LocalDateTime.now().minusHours(1).plusMinutes(30), // Ends 1 hour 30 minutes ago
                AuctionSessionType.ASCENDING,
                500,
                auctionRequestRepository.findAuctionRequestByTitle("Shusui Auction").getAuction_request_id(),
                accountRepository.findAccountByUsername("staff").getUser_id());
        //----------------------------------------------------------------------------------
        for (AuctionSessionRequestDTO auctionSessionRequestDTO : auctionSessionRequestDTOList) {
            createAuctionSession(auctionSessionRequestDTO, true);
        }
    }

    public void customBid() {
        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member").getUser_id());
        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member").getUser_id());
        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member").getUser_id());

        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member").getUser_id());
        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member1").getUser_id());

        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member3").getUser_id());

        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Hi Utsuri Special Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member").getUser_id());

        createDsBidRequestDTO(150,
                auctionSessionRepository.findAuctionSessionByTitle("Hi Utsuri Special Auction").getAuctionSessionId(), accountRepository.findAccountByUsername("member1").getUser_id());


        for (DsBidRequestDTO dsBidRequestDTO : dsBidRequestDTOList) {
            createBid(dsBidRequestDTO);
        }
        auctionSessionService.closeAuctionSession(auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction"));
    }

    public void pastBid() {
        Long kikokuryuAuctionId = auctionSessionRepository.findAuctionSessionByTitle("Kikokuryu Exclusive Auction").getAuctionSessionId();
        Long hiUtsuriAuctionId = auctionSessionRepository.findAuctionSessionByTitle("Hi Utsuri Special Auction").getAuctionSessionId();
        Long shusuiAuctionId = auctionSessionRepository.findAuctionSessionByTitle("Shusui Auction Event").getAuctionSessionId();
        List<Account> memberAccounts = accountRepository.findAccountsByRoleEnum(AccountRoleEnum.MEMBER);
        List<Double> bidAmounts = List.of(150.00, 200.00, 250.00, 300.00);

        for (Account member : memberAccounts) {
            Long memberId = member.getUser_id();
            for (double bidAmount : bidAmounts) {
                // Add bids to Kikokuryu Exclusive Auction
                DsBidRequestDTO bidRequestKikokuryu = createDsBidRequestDTO(bidAmount, kikokuryuAuctionId, memberId);
                createBid(bidRequestKikokuryu);

                // Add bids to Hi Utsuri Special Auction
                DsBidRequestDTO bidRequestHiUtsuri = createDsBidRequestDTO(bidAmount, hiUtsuriAuctionId, memberId);
                createBid(bidRequestHiUtsuri);

                // Add bids to Shusui Auction Event
                DsBidRequestDTO bidRequestShusui = createDsBidRequestDTO(bidAmount, shusuiAuctionId, memberId);
                createBid(bidRequestShusui);
            }
        }
    }

    public Date getPastDate(int daysInThePast) {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(daysInThePast);
        return Date.from(pastDate.atZone(ZoneId.systemDefault()).toInstant());
    }

}
