package com.quickblox.sample.pushnotifications;

import android.util.Log;
import android.widget.Toast;

import com.quickblox.messages.services.QBPushManager;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;
import com.quickblox.sample.pushnotifications.utils.Consts;

public class App extends CoreApp {

    private static App instance;

    private int currentUserId;
    public static boolean playServicesAbility = true;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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
                if (resultCode >= 0) {
                    playServicesAbility = false;
                }
                Log.d("AppMessage", "onSubscriptionError playServicesAbility= " + playServicesAbility);
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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