package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Account;
import com.group10.koiauction.entity.AuctionRequest;
import com.group10.koiauction.entity.enums.AuctionRequestStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AuctionRequestRepository extends JpaRepository<AuctionRequest, Long> {
    @Query("SELECT U FROM AuctionRequest U WHERE U.auction_request_id = ?1")
    AuctionRequest findByAuctionRequestId(@Param("auction_request_id") Long id);

    @Query("SELECT  a FROM AuctionRequest a WHERE a.account.username IN (:usernameList) ")
    List<AuctionRequest> findByUsernameList(@Param("usernameList") List<String> usernameList);

    @Query("SELECT U FROM AuctionRequest U WHERE U.status = ?1")
    List<AuctionRequest> findByStatus(@Param("status") AuctionRequestStatusEnum status);

    @Query("SELECT U FROM AuctionRequest U WHERE U.account.user_id = :koiBreederId")
    List<AuctionRequest> findByBreederId(@Param("koiBreederId") Long koiBreederId);

    AuctionRequest findAuctionRequestByTitle(String title);

    @Query("SELECT a FROM AuctionRequest a WHERE a.account.user_id = :koiBreederId")
    Page<AuctionRequest> findAllAuctionRequestOfCurrentBreederPagination(@Param("koiBreederId") Long koiBreederId, Pageable pageable);

    @Query("SELECT a FROM AuctionRequest a ")
    Page<AuctionRequest> findAllAuctionRequestPaginationForStaff(Pageable pageable);

    @Query("SELECT a FROM AuctionRequest a " +
            "WHERE (a.status IN (:statuses) OR :statuses IS NULL) and " +
            "      ( a.account IN (:breeders) OR :breeders IS NULL ) ")
    Page<AuctionRequest> findAllAuctionRequestPaginationForStaffFilter(Pageable pageable,
                                                                       @Param("statuses") List<AuctionRequestStatusEnum> statuses, @Param("breeders") List<Account> breeders);

    @Query("SELECT  a FROM  AuctionRequest a WHERE a.status IN (:statuses)")
    Page<AuctionRequest> findAllAuctionRequestPaginationForStaffByStatus(Pageable pageable,
                                                                         @Param("statuses") List<AuctionRequestStatusEnum> statusList);


    @Query("SELECT U FROM AuctionRequest U WHERE U.status = :status")
    Page<AuctionRequest> findByStatus(@Param("status") AuctionRequestStatusEnum status, Pageable pageable);

    @Query("SELECT u FROM AuctionRequest u WHERE (u.status = :status OR :status IS NULL) " +
            "AND (:startDate IS NULL OR DATE(u.createdDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(u.createdDate) <= DATE(:endDate)) " +
            "AND u.account = :koiBreeder")
    Page<AuctionRequest> filterAuctionRequestForKoiBreeder(@Param("status") AuctionRequestStatusEnum status,
                                                           @Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("koiBreeder") Account koiBreeder,
                                                           Pageable pageable);

    @Query("SELECT u FROM AuctionRequest u WHERE (u.status = :status OR :status IS NULL) " +
            "AND (:startDate IS NULL OR DATE(u.createdDate) >= DATE(:startDate)) " +
            "AND (:endDate IS NULL OR DATE(u.createdDate) <= DATE(:endDate)) " +
            "AND u.account = :koiBreeder")
    Page<AuctionRequest> filterAuctionRequestForManagerFilter(@Param("status") AuctionRequestStatusEnum status,
                                                           @Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("koiBreeder") Account koiBreeder,
                                                           Pageable pageable);

}
