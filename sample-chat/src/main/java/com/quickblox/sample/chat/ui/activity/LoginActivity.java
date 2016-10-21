package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends CoreBaseActivity {

    private ListView userListView;
    private RadioGroup serversRadioGroup;
    private EditText portEditText;
    Button recreateConnectionButton;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userListView = _findViewById(R.id.list_login_users);
        serversRadioGroup = _findViewById(R.id.servers_radio_group);
        portEditText = _findViewById(R.id.port_text_dit);
        recreateConnectionButton = _findViewById(R.id.recreate_connection_button);
        recreateConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreateConnection();
            }
        });

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, userListView, false);
        listHeader.setText(R.string.login_select_user_for_login);

        userListView.addHeaderView(listHeader, null, false);
        userListView.setOnItemClickListener(new OnUserLoginItemClickListener());
    }


    private void recreateConnection(){
        if (!isServerDataValid()){
            Toaster.shortToast("Please enter chat port and select server");
            return;
        }

        SharedPreferencesUtil.saveChatPort(Integer.parseInt(String.valueOf(portEditText.getText())));
        initAppCredentials(serversRadioGroup.getCheckedRadioButtonId());

        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_creating_new_connection);

        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                buildUsersList();
                ProgressDialogFragment.hide(getSupportFragmentManager());
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                Toaster.shortToast("Error creating new connection. Error: " + e.getMessage());
            }
        });
    }

    private boolean isServerDataValid(){
        int chatPort;
        try {
            chatPort = Integer.parseInt(String.valueOf(portEditText.getText()));
        } catch (NumberFormatException e){
            return false;
        }

        boolean portValid = chatPort > 0 && chatPort <= 65535;

        boolean serverSelected = serversRadioGroup.getCheckedRadioButtonId() != -1;

        return portValid && serverSelected;
    }

    public void initAppCredentials(int selectedServer){
        if (selectedServer == R.id.shared_server_rario_button) {
            QBSettings.getInstance().init(getApplicationContext(), Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
            QBSettings.getInstance().setAccountKey(Consts.QB_ACCOUNT_KEY);

            QBSettings.getInstance().setEndpoints(Consts.QB_API_DOMAIN, Consts.QB_CHAT_DOMAIN, ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        } else if (selectedServer == R.id.stage5_server_radio_button){
            QBSettings.getInstance().init(getApplicationContext(), Consts.STAGE_APP_ID, Consts.STAGE_AUTH_KEY, Consts.STAGE_AUTH_SECRET);
            QBSettings.getInstance().setAccountKey(Consts.STAGE_ACCOUNT_KEY);

            QBSettings.getInstance().setEndpoints(Consts.STAGE_API_DOMAIN, Consts.STAGE_CHAT_DOMAIN, ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }

    private void buildUsersList() {
        List<String> tags = new ArrayList<>();
        tags.add(Consts.QB_USERS_TAG);

        QBUsers.getUsersByTags(tags, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                UsersAdapter adapter = new UsersAdapter(LoginActivity.this, result);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onError(QBResponseException e) {
                ErrorUtils.showSnackbar(userListView, R.string.login_cant_obtain_users, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                buildUsersList();
                            }
                        });
            }
        });
    }

    private void login(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                SharedPreferencesUtil.saveQbUser(user);

                DialogsActivity.start(LoginActivity.this);
                finish();

                ProgressDialogFragment.hide(getSupportFragmentManager());
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                ErrorUtils.showSnackbar(userListView, R.string.login_chat_login_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login(user);
                            }
                        });
            }
        });
    }

    private class OnUserLoginItemClickListener implements AdapterView.OnItemClickListener {

        public static final int LIST_HEADER_POSITION = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == LIST_HEADER_POSITION) {
                return;
            }


            final QBUser user = (QBUser) parent.getItemAtPosition(position);
            // We use hardcoded password for all users for test purposes
            // Of course you shouldn't do that in your app
            user.setPassword(Consts.QB_USERS_PASSWORD);

            login(user);
        }

    }
}
