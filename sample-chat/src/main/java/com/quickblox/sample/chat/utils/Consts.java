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

    //stage5 configs
//    String QB_APP_ID = "2";
//    String QB_AUTH_KEY = "SyGPM5MGzkK6Fkz";
//    String QB_AUTH_SECRET = "8LXxKweOTQpqZcc";
//    String QB_ACCOUNT_KEY = "E5beJD2ixxKztWszXZJC";
//    String API_DOMAIN = "https://apistage5.quickblox.com";
//    String CHAT_DOMAIN = "chatstage5.quickblox.com";

    //stage1 configs
    String QB_APP_ID = "6";
    String QB_AUTH_KEY = "mCAE4KBs9DAtKMv";
    String QB_AUTH_SECRET = "a86rse7eyD6NL3Y";
    String QB_ACCOUNT_KEY = "oCuPpybWb2FU3XksECGP";
    String API_DOMAIN = "https://apistage1.quickblox.com";
    String CHAT_DOMAIN = "chatstage1.quickblox.com";
//
    //QuickBlox QA configs
//    String QB_APP_ID = "10";
//    String QB_AUTH_KEY = "6jBqsOAfEHJswv3";
//    String QB_AUTH_SECRET = "yWKGgVmtX6ZqmCg";
//    String QB_USERS_TAG = "qbusers";

    //FieldBit configs
//    String QB_APP_ID = "1";
//    String QB_AUTH_KEY = "khz5UR63NTfqMCC";
//    String QB_AUTH_SECRET = "6hZFGb6c8QMZY4-";
//    String QB_USERS_TAG = "webrtcusers";

//    String QB_ACCOUNT_KEY = "v6rMo2bJU8THie9ousC1";
//    String API_DOMAIN = "https://apifieldbit.quickblox.com";
//    String CHAT_DOMAIN = "chat.fieldbit.net";

    String QB_USERS_TAG = "webrtcusers";
    String QB_USERS_PASSWORD = "x6Bt0VDy5";

    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
}