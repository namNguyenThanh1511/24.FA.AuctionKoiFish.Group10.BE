package com.group10.koiauction.repository;

import com.group10.koiauction.entity.Transaction;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.entity.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT U FROM  Transaction  U WHERE U.id = ?1")
    Transaction findTransactionById(@Param("id") Long id);

    @Query("SELECT t FROM Transaction t WHERE t.from.user_id = :userId OR t.to.user_id = :userId")
    List<Transaction> findTransactionByUserId(Long userId);

    @Query("SELECT t.auctionSession.auctionSessionId , t.amount FROM Transaction t WHERE t.to.username = 'manager' ")
    List<Object[]> findRevenueOfAuctionSession();


 //    @Query("SELECT YEAR(s.date) , MONTH(s.date) ,DAY(s.date) , MAX(s.balance) FROM SystemProfit s GROUP BY YEAR(s" +
//            ".date) , MONTH(s.date) ,DAY(s.date) ")
//    List<Object[]> calculateDailySystemRevenue();
//
//    @Query("SELECT YEAR(s.date) , MONTH(s.date) , MAX(s.balance) FROM SystemProfit s GROUP BY YEAR(s.date) , MONTH(s.date)")
//    List<Object[]> calculateMonthLySystemRevenue();

    @Query("SELECT YEAR(t.createAt),MONTH(t.createAt),DAY(t.createAt),SUM(t.amount) FROM Transaction t WHERE t.type = :feeTransfer GROUP BY YEAR" +
            "(t.createAt),MONTH(t.createAt),DAY(t.createAt) ")
    List<Object[]> calculateDailySystemRevenue(@Param("feeTransfer") TransactionEnum feeTransfer);

    @Query("SELECT YEAR(t.createAt),MONTH(t.createAt),SUM(t.amount) FROM Transaction t WHERE t.type = :feeTransfer GROUP BY YEAR" +
            "(t.createAt),MONTH(t.createAt),DAY(t.createAt) ")
    List<Object[]> calculateMonthLySystemRevenue(@Param("feeTransfer") TransactionEnum feeTransfer);

    @Query("SELECT t FROM Transaction t WHERE "
            + "(:transactionType IS NULL OR t.type = :transactionType) "
            + "AND (:fromUserId IS NULL OR t.from.user_id = :fromUserId ) "
            + "AND (:toUserId IS NULL OR t.from.user_id = :toUserId ) "
            + "AND (:startDate IS NULL OR t.createAt >= :startDate) "
            + "AND (:endDate IS NULL OR t.createAt <= :endDate) "
            + "AND (:minAmount IS NULL OR t.amount >= :minAmount) "
            + "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) "
            + "AND (:auctionSessionId IS NULL OR t.auctionSession.auctionSessionId = :auctionSessionId)")
    Page<Transaction> filterTransactions(
            @Param("transactionType") TransactionEnum transactionType,
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            @Param("auctionSessionId") Long auctionSessionId,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.from.user_id = :userId OR t.to.user_id = :userId) " +
            "AND (:transactionType IS NULL OR t.type = :transactionType) " +
            "AND (:startDate IS NULL OR t.createAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createAt <= :endDate)")
    Page<Transaction> findTransactionsByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("transactionType") TransactionEnum transactionType,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);

}









