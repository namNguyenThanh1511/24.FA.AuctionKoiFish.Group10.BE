package com.group10.koiauction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationFCM {
    private String title;
    private String message;
    String fcmToken;//send to who , front end will send fcm token after login
}
