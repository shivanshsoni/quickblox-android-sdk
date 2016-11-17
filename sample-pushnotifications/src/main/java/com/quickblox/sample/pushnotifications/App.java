package com.quickblox.sample.pushnotifications;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.pushnotifications.utils.Consts;

public class App extends CoreApp {

    private static App instance;

    private int currentUserId;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        QBPushNotifications.getSubscriptions();
        ActivityLifecycle.init(this);
        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);
        QBPushManager.getInstance().addListener(new QBPushManager.QBSubscribeListener() {
            @Override
            public void onSubscriptionCreated() {
                Log.d("AppMessage", "onSubscriptionCreated");
            }

            @Override
            public void onSubscriptionError(final Exception e, int resultCode) {
                Log.d("AppMessage", "onSubscriptionError" + e);

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onUnsubscribeSuccess() {

            }

            @Override
            public void onUnsubscribeError(Exception e) {

            }
        });
    }

    public static synchronized App getInstance() {
        return instance;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }
}