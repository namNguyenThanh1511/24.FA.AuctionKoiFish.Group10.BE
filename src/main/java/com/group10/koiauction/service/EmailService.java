package com.group10.koiauction.service;


import com.group10.koiauction.entity.AuctionSession;
import com.group10.koiauction.model.response.EmailDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sentEmail(EmailDetail emailDetail) {
        try{
            Context context = new Context();
            context.setVariable("name", emailDetail.getAccount().getEmail());
            context.setVariable("button", "Go to website");
            context.setVariable("link", emailDetail.getLink());

            String tmeplate = templateEngine.process("welcome-template", context);

            // Creating a simple mail message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            // Setting up necessary details
            mimeMessageHelper.setFrom("customerserviceinkmelo@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setText(tmeplate, true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            javaMailSender.send(mimeMessage);
        }catch (MessagingException e){
            System.out.println("Error sending email");
        }
    }


    public void sentEmailBreeder(EmailDetail emailDetail) {
        try{
            Context context = new Context();
            context.setVariable("name", emailDetail.getAccount().getEmail());
            context.setVariable("button", "Go to website");
            context.setVariable("link", emailDetail.getLink());

            String tmeplate = templateEngine.process("welcome-breeder-template", context);

            // Creating a simple mail message
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            // Setting up necessary details
            mimeMessageHelper.setFrom("customerserviceinkmelo@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setText(tmeplate, true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            javaMailSender.send(mimeMessage);
        }catch (MessagingException e){
            System.out.println("Error sending email");
        }
    }

    public void sendAuctionSessionCreatedEmail(EmailDetail emailDetail, AuctionSession auctionSession) {
        try {
            Context context = new Context();

            // Format LocalDateTime to yyyy-MM-dd
            LocalDateTime startDateTime = auctionSession.getStartDate();
            LocalDateTime endDateTime = auctionSession.getEndDate();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            context.setVariable("startDate", startDateTime.format(formatter)); // Format startDate
            context.setVariable("endDate", endDateTime.format(formatter)); // Format endDate
            context.setVariable("auctionName", auctionSession.getTitle());

            // Format Starting Bid (VND)
            double startingBid = auctionSession.getStartingPrice();
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            context.setVariable("startingBid", currencyFormat.format(startingBid)); // Format VND
            context.setVariable("auctionType", auctionSession.getAuctionType());
            context.setVariable("breederName", auctionSession.getKoiFish().getAccount().getFirstName() + " " + auctionSession.getKoiFish().getAccount().getLastName());

            // Link to the auction session
            context.setVariable("auctionLink", emailDetail.getLink());

            // Process template
            String template = templateEngine.process("auctionsession-created", context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("customerserviceinkmelo@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setText(template, true);
            mimeMessageHelper.setSubject("New Auction Session Created!");

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("Error sending auction session email");
        }
    }

}
