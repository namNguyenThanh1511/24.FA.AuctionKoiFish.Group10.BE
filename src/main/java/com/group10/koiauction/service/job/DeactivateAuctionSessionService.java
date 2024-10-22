package com.group10.koiauction.service.job;

import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.repository.AuctionSessionRepository;
import com.group10.koiauction.service.AuctionSessionService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DeactivateAuctionSessionService implements Job {

    @Autowired
    private AuctionSessionService auctionSessionService;

    @Autowired
    private AuctionSessionRepository auctionSessionRepository;

    @Transactional
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String auctionSessionId = jobExecutionContext.getJobDetail().getJobDataMap().getString("auctionSessionId");
        Long longAuctionSessionId = Long.parseLong(auctionSessionId);
        AuctionSession auctionSession = auctionSessionRepository.findById(longAuctionSessionId).orElseThrow(() -> new JobExecutionException("AuctionSession not found for ID: " + auctionSessionId));
        if (auctionSession.getStatus() == AuctionSessionStatus.ONGOING) {
            try {
                auctionSessionService.closeAuctionSession(auctionSession);
                System.out.println("AuctionSession " + auctionSessionId + " has been closed.");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

        }
    }
}
