package com.quickblox.sample.chat.utils;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.utils.ResourceUtils;

public interface Consts {
    // In GCM, the Sender ID is a project ID that you acquire from the API console
    String GCM_SENDER_ID = "761750217637";

//    String QB_APP_ID = "92";
//    String QB_AUTH_KEY = "wJHdOcQSxXQGWx5";
//    String QB_AUTH_SECRET = "BTFsj7Rtt27DAmT";
//    String QB_ACCOUNT_KEY = "rz2sXxBt5xgSxGjALDW6";
//
    //stage5 configs
    String QB_APP_ID = "5";
    String QB_AUTH_KEY = "8VSwXgcwBAjgmGx";
    String QB_AUTH_SECRET = "HbW4MHU76DcyHqu";
    String QB_ACCOUNT_KEY = "dhEZCzD6rPuqXU8BWDqM";
    String API_DOMAIN = "https://apistage5.quickblox.com";
    String CHAT_DOMAIN = "chatstage5.quickblox.com";

    String QB_USERS_TAG = "webrtcusers";
    String QB_USERS_PASSWORD = "x6Bt0VDy5";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
}