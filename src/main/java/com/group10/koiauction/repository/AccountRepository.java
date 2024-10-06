package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT U FROM Account U WHERE U.user_id = ?1")
    Account findByUser_id(@Param("user_id") Long user_id);// @Param("mapping voi query")

    @Query("SELECT U FROM Account  U  WHERE U.username = ?1 AND U.password = ?2 ")
    Account findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Account findByUsername(String username);

    Account findAccountByEmail(String email);

    Account findAccountByPhoneNumber(String phoneNumber);


}
