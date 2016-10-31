package com.quickblox.sample.pushnotifications.gcm;

import com.quickblox.messages.services.gcm.QBGcmPushListenerService;

public class GcmPushListenerService extends QBGcmPushListenerService {
    private String type = "GCM";

    @Override
    protected void sendPushMessage(String message) {
        super.sendPushMessage(message + " (type= " + type +")");
    }

}