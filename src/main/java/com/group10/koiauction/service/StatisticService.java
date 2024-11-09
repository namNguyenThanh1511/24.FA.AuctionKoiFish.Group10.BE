package com.group10.koiauction.service;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.entity.enums.TransactionEnum;
import com.group10.koiauction.repository.AccountRepository;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.repository.BidRepository;
import com.group10.koiauction.repository.TransactionRepository;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    AuctionSessionRepository auctionSessionRepository;

    @Autowired
    TransactionRepository transactionRepository;


    public Map<String,Object> getStatistic(){
        Map<String,Object> map = new HashMap<>();
        long countMember = accountRepository.countAccountByRole(AccountRoleEnum.MEMBER);
        long countKoiBreeder = accountRepository.countAccountByRole(AccountRoleEnum.KOI_BREEDER);
        long countStaff = accountRepository.countAccountByRole(AccountRoleEnum.STAFF);
        long countNumberOfCompletedAuctionSession =
                auctionSessionRepository.countdAuctionSessionByStatus(List.of(AuctionSessionStatus.COMPLETED,
                        AuctionSessionStatus.DRAWN));
        long countNumberOfAllAuctionSessionExceptUpcomingAndOngoing =
                auctionSessionRepository.countdAuctionSessionExceptStatus(List.of(AuctionSessionStatus.UPCOMING,
                        AuctionSessionStatus.ONGOING));
        double averageNumOfBidPerAuctionSession = calculateAvgBidCountPerAuctionSession();

        List<Object[]> topAuctionSession = bidRepository.findTopTrendingAuctionSession(2);
        List<Object[]> topAuctionSessionNumberOfBid = bidRepository.findTopAuctionSessionNumberOfBid(2) ;
        List<Object[]> topBidderAmount = bidRepository.findTopBidderAmount(2);
        List<Object[]> topBidderNumberOfBid = bidRepository.findTopBidderNumberOfBid(2);
        List<Object[]> topVarieties = bidRepository.findTopVarieties(5);
        List<Object[]> allAuctionSessionRevenue = transactionRepository.findRevenueOfAuctionSession();
        List<Object[]> dailySystemRevenue = transactionRepository.calculateDailySystemRevenue(TransactionEnum.FEE_TRANSFER);
        List<Object[]> monthlySystemRevenue = transactionRepository.calculateMonthLySystemRevenue(TransactionEnum.FEE_TRANSFER);

        List<Map<String,Object>> topAuctionSessionList = new ArrayList<>();
        List<Map<String,Object>> topAuctionSessionNumberOfBidList = new ArrayList<>();
        List<Map<String,Object>> topBidderAmountList = new ArrayList<>();
        List<Map<String,Object>> topBidderNumberOfBidList = new ArrayList<>();
        List<Map<String,Object>> topVarietiesList = new ArrayList<>();
        List<Map<String,Object>> allAuctionSessionRevenueList = new ArrayList<>();
        List<Map<String,Object>> dailySystemRevenueList = new ArrayList<>();
        List<Map<String,Object>> monthlySystemRevenueList = new ArrayList<>();

        for(Object[] row : topAuctionSession){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Auction Session ID",row[0]);
            rowMap.put("Total participant(s)",row[1]);
            topAuctionSessionList.add(rowMap);
        }

        for (Object[] row : topAuctionSessionNumberOfBid){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Auction Session ID",row[0]);
            rowMap.put("Total number of bid(s)",row[1]);
            topAuctionSessionNumberOfBidList.add(rowMap);
        }

        for(Object[] row : topBidderAmount){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("User ID",row[0]);
            rowMap.put("Username",row[1]);
            rowMap.put("Total bid amount",row[2]);
            topBidderAmountList.add(rowMap);
        }

        for(Object[] row : topBidderNumberOfBid){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("User ID",row[0]);
            rowMap.put("Username",row[1]);
            rowMap.put("Total number of bids",row[2]);
            topBidderNumberOfBidList.add(rowMap);
        }
        for (Object[] row : topVarieties){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Variety ID",row[0]);
            rowMap.put("Name",row[1]);
            rowMap.put("Number of participant(s)",row[2]);
            topVarietiesList.add(rowMap);
        }
        for(Object[] row : allAuctionSessionRevenue){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Auction Session ID",row[0]);
            rowMap.put("Total revenue",row[1]);
            allAuctionSessionRevenueList.add(rowMap);
        }
        for (Object[] row : dailySystemRevenue){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Year",row[0]);
            rowMap.put("Month",row[1]);
            rowMap.put("Day",row[2]);
            rowMap.put("Balance",row[3]);
            dailySystemRevenueList.add(rowMap);
        }
        for (Object[] row : monthlySystemRevenue){
            Map<String,Object> rowMap = new HashMap<>();
            rowMap.put("Year",row[0]);
            rowMap.put("Month",row[1]);
            rowMap.put("Balance",row[2]);
            monthlySystemRevenueList.add(rowMap);
        }


        map.put("Total members",countMember);
        map.put("Total koi breeders",countKoiBreeder);
        map.put("Total staffs",countStaff);
        map.put("Top Auction Sessions",topAuctionSessionList);
        map.put("Top Auction Sessions number of bids",topAuctionSessionNumberOfBidList);
        map.put("Total number of Auction Sessions",auctionSessionRepository.count());
        map.put("Total number of completed auction session",countNumberOfCompletedAuctionSession);
        map.put("Total number of auction session except upcoming and ongoing",
                countNumberOfAllAuctionSessionExceptUpcomingAndOngoing);
        map.put("Successful rate of all auction session (except upcoming )",
                (float)countNumberOfCompletedAuctionSession/countNumberOfAllAuctionSessionExceptUpcomingAndOngoing);
        map.put("Average number of bids per auction session",averageNumOfBidPerAuctionSession);
        map.put("Top Bidder Amounts",topBidderAmountList);
        map.put("Top Bidder Number of Bids",topBidderNumberOfBidList);
        map.put("Top Varieties",topVarietiesList);
        map.put("Auction Session Revenue",allAuctionSessionRevenueList);
        map.put("Daily System Revenue",dailySystemRevenueList);
        map.put("Monthly System Revenue",monthlySystemRevenueList);
        return map;

    }
    public double calculateAvgBidCountPerAuctionSession(){
        List<Long> bidCounts = bidRepository.findBidCountsPerSession();
        return bidCounts.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
    }

}
