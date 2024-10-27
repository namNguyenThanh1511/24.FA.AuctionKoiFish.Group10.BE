package com.group10.koiauction.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group10.koiauction.entity.Account;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.management.relation.Role;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("SELECT U FROM Account U WHERE U.user_id = ?1")
    Account findByUser_id(@Param("user_id") Long user_id);// @Param("mapping voi query")

    @Query("SELECT U FROM Account  U  WHERE U.username = ?1 AND U.password = ?2 ")
    Account findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Query("SELECT U FROM Account U WHERE U.roleEnum = ?1")
    Account findAccountByRole(@Param("roleEnum") AccountRoleEnum role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    Account findByUsername(String username);

    Account findAccountByEmail(String email);

    @Query("SELECT U FROM Account U WHERE U.phoneNumber = :phoneNumber")
    Optional<Account> findAccountByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    Account findAccountByUsername(String username);

    List<Account> findAccountsByRoleEnum(AccountRoleEnum role);

    Page<Account> findAccountsByRoleEnum(AccountRoleEnum role, Pageable pageable);

    @Query("SELECT COUNT(a.user_id) FROM  Account  a WHERE a.roleEnum = :role")
    long countAccountByRole(@Param("role") AccountRoleEnum role);

}
