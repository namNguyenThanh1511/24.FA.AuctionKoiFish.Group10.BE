package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT U FROM Account U WHERE U.user_id = ?1")
    Account findByUser_id(@Param("user_id") Long user_id);// @Param("mapping voi query")
}
