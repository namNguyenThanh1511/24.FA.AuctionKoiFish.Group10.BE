package com.group10.koiauction.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.group10.koiauction.entity.Account;
import com.group10.koiauction.model.NotificationFCM;
import org.mockserver.model.Not;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public FirebaseMessaging firebaseMessaging;

    public NotificationService(FirebaseApp firebaseApp) {
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    public void sendNotification(NotificationFCM notificationFCM) {
        Notification notificationFireBase = Notification.builder()
                .setTitle(notificationFCM.getTitle())
                .setBody(notificationFCM.getMessage())
                .build();
        Message notificationFireBaseMessage =
                Message.builder()
                        .setNotification(notificationFireBase)
                        .setToken(notificationFCM.getFcmToken())//to identify receiver
                        .build();
        try{
            firebaseMessaging.send(notificationFireBaseMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public void sendNotificationToAccount(NotificationFCM notificationFCM , Account account) {
        Notification notificationFireBase = Notification.builder()
                .setTitle(notificationFCM.getTitle())
                .setBody(notificationFCM.getMessage())
                .build();
        Message notificationFireBaseMessage =
                Message.builder()
                        .setNotification(notificationFireBase)
                        .setToken(account.getFcmToken())//to identify receiver
                        .build();
        try{
            firebaseMessaging.send(notificationFireBaseMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
