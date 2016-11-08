package com.quickblox.sample.pushnotifications.gcm;

import android.os.Bundle;

import com.quickblox.messages.services.gcm.QBGcmPushListenerService;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.activities.SplashActivity;

public class GcmPushListenerService extends QBGcmPushListenerService {

    @Override
    protected void sendPushMessage(Bundle data, String from, String message) {
        super.sendPushMessage(data, from, message);
        showNotification(message);
    }

    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, 1);
    }
}