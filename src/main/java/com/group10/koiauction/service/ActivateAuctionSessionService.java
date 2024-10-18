package com.group10.koiauction.service;

import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.entity.enums.AuctionSessionStatus;
import com.group10.koiauction.repository.AuctionSessionRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivateAuctionSessionService implements Job {

    @Autowired
    AuctionSessionRepository auctionSessionRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String auctionSessionId = jobExecutionContext.getJobDetail().getJobDataMap().getString("auctionSessionId");
        Long longAutionID = Long.parseLong(auctionSessionId);
        AuctionSession auctionSession = auctionSessionRepository.findById(longAutionID)
                .orElseThrow(() -> new JobExecutionException("AuctionSession not found for ID: " + auctionSessionId));

        if (auctionSession.getStatus() == AuctionSessionStatus.UPCOMING) {
            auctionSession.setStatus(AuctionSessionStatus.ONGOING);
            auctionSessionRepository.save(auctionSession);
            System.out.println("AuctionSession " + auctionSessionId + " has been activated.");
        }

    }
}
