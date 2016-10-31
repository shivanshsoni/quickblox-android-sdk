package com.quickblox.sample.pushnotifications.fcm;


import com.quickblox.messages.services.fcm.QBFcmPushListenerService;

public class FcmPushListenerService extends QBFcmPushListenerService {
    private static final String TAG = FcmPushListenerService.class.getSimpleName();
    private String type = "FCM";

    @Override
    protected void sendPushMessage(String message) {
        super.sendPushMessage(message + " (type= " + type +")");
    }
}