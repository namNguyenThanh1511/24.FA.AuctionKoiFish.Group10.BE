package com.group10.koiauction.api;

import com.group10.koiauction.model.NotificationFCM;
import com.group10.koiauction.model.request.UpdateFCMRequestDTO;
import com.group10.koiauction.model.response.AccountResponse;
import com.group10.koiauction.service.AuthenticationService;
import com.group10.koiauction.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "api")
@RestController
@RequestMapping("/api/notification")
public class NotificationAPI {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("notification")
    public void sendNotification(@RequestBody NotificationFCM notificationFCM) {
        notificationService.sendNotification(notificationFCM);
    }


}
