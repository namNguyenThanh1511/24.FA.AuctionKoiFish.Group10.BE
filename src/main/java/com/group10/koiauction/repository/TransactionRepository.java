package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT U FROM  Transaction  U WHERE U.id = ?1")
    Transaction findTransactionById(@Param("id") Long id);

    @Query("SELECT t FROM Transaction t WHERE t.from.user_id = :userId OR t.to.user_id = :userId")
    List<Transaction> findTransactionByUserId(Long userId);

    @Query("SELECT t.auctionSession.auctionSessionId , t.amount FROM Transaction t WHERE t.to.username = 'manager' ")
    List<Object[]> findRevenueOfAuctionSession();

    @Query("SELECT YEAR(s.date) , MONTH(s.date) , s.balance FROM SystemProfit s")
    List<Object[]> calculateSystemRevenue();

}









