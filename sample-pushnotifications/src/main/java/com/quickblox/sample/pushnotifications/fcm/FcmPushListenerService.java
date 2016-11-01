package com.quickblox.sample.pushnotifications.fcm;


import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.activities.SplashActivity;

public class FcmPushListenerService extends QBFcmPushListenerService {
    private static final String TAG = FcmPushListenerService.class.getSimpleName();

    @Override
    protected void sendPushMessage(String message) {
        super.sendPushMessage(message);
        showNotification(message);
    }

    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, SplashActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, 1);
    }
}