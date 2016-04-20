package com.quickblox.simplesample.pushnotifications.gcm;

import com.quickblox.sample.core.gcm.CoreGcmPushInstanceIDService;
import com.quickblox.simplesample.pushnotifications.utils.Consts;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    @Override
    protected String getSenderId() {
        return Consts.GCM_SENDER_ID;
    }
}
