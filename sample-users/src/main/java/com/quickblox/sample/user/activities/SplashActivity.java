package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends CoreSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isSignedIn()){
            loadUUserById(QBSessionManager.getInstance().getSessionParameters().getUserId());
        } else {
            proceedToTheNextActivity();
        }
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        UsersListActivity.start(this);
        finish();
    }

    private boolean isSignedIn(){
        return QBSessionManager.getInstance().getSessionParameters() != null && QBSessionManager.getInstance().getSessionParameters().getUserId() != 0;
    }

    private void loadUUserById(final int userId){
        QBUsers.getUser(userId).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                DataHolder.getInstance().setSignInQbUser(result);
                proceedToTheNextActivity();
            }

            @Override
            public void onError(QBResponseException e) {
                showSnackbarError(null, R.string.errors, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadUUserById(userId);
                    }
                });
            }
        });

    }
}