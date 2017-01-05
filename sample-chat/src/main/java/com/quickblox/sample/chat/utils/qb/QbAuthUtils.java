package com.quickblox.sample.chat.utils.qb;

import android.text.TextUtils;

import com.quickblox.auth.session.QBSessionManager;

import java.util.Date;

public class QbAuthUtils {

    public static boolean isSessionActive() {
        String token = QBSessionManager.getInstance().getToken();
        Date expirationDate = QBSessionManager.getInstance().getTokenExpirationDate();

        return !TextUtils.isEmpty(token) && System.currentTimeMillis() < expirationDate.getTime();
    }

}
