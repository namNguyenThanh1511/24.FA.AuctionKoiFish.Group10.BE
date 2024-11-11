package com.group10.koiauction.service;


import com.group10.koiauction.constant.MappingURL;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    public void sendAuctionWinnerEmail(EmailDetail emailDetail, AuctionSession auctionSession) {
        try {
            Context context = new Context();

            // Auction details
            context.setVariable("winnerName", auctionSession.getWinner().getFirstName() + " " + auctionSession.getWinner().getLastName());
            context.setVariable("auctionName", auctionSession.getTitle());

            // Format winning bid to VND
            NumberFormat vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedWinningBid = vndFormat.format(auctionSession.getCurrentPrice());
            context.setVariable("winningBid", formattedWinningBid);

            context.setVariable("auctionType", auctionSession.getAuctionType());

            ZonedDateTime vietnamCurrentTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            context.setVariable("endDate", vietnamCurrentTime.format(formatter));

            // Link to the auction session details
            String auctionLink = MappingURL.BASE_URL_LOCAL + "auctions/" + auctionSession.getAuctionSessionId();
            context.setVariable("auctionLink", auctionLink);

            // Process the template
            String template = templateEngine.process("auction-winner-notification", context);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("customerserviceinkmelo@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setSubject("Congratulations on Winning the Auction!");
            mimeMessageHelper.setText(template, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("Error sending auction winner notification email");
        }
    }

    public void sendBuyNowSuccessEmail(EmailDetail emailDetail, AuctionSession auctionSession) {
        try {
            // Create context for Thymeleaf template engine
            Context context = new Context();

            // Add user and product details to the context
            context.setVariable("userName", emailDetail.getAccount().getFirstName() + " " + emailDetail.getAccount().getLastName());
            context.setVariable("auctionSession", auctionSession.getTitle());

            // Format Buy Now price to VND
            NumberFormat vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = vndFormat.format(auctionSession.getBuyNowPrice());
            context.setVariable("buyNowPrice", formattedPrice);

            // Add purchase date
            ZonedDateTime vietnamCurrentTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            context.setVariable("purchaseDate", vietnamCurrentTime.format(formatter));

            // Prepare email template
            String template = templateEngine.process("buy-now-success", context);

            // Link to the auction session details
            String auctionLink = MappingURL.BASE_URL_LOCAL + "auctions/" + auctionSession.getAuctionSessionId();
            context.setVariable("auctionLink", auctionLink);

            // Create MIME message and set up its properties
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("customerserviceinkmelo@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getAccount().getEmail());
            mimeMessageHelper.setSubject("Buy Now Transaction Successful!");
            mimeMessageHelper.setText(template, true);

            // Send email
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("Error sending Buy Now success email");
        }
    }

}
