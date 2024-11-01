package com.group10.koiauction.repository;

import com.group10.koiauction.entity.WithDrawRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WithDrawRequestRepository extends JpaRepository<WithDrawRequest, Long> {


    @Query("SELECT w FROM WithDrawRequest w WHERE w.user.user_id = :userId OR :userId IS NULL")
    Page<WithDrawRequest> findWithDrawRequestByCustomerIdPagination(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM WithDrawRequest w WHERE w.user.user_id = :userId OR :userId IS NULL")
    Page<WithDrawRequest> findWithDrawRequestByCurrentUserPagination(@Param("userId") Long userId, Pageable pageable);

}
