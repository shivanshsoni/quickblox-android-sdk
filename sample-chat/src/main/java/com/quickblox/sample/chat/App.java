package com.quickblox.sample.chat;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBHttpConnectionConfig;
import com.quickblox.core.ServiceZone;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.ActivityLifecycle;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class App extends CoreApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.30.30.1", 3128));
        QBHttpConnectionConfig.setProxy(proxy);

        initCredentials(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET, Consts.QB_ACCOUNT_KEY);

        QBSettings.getInstance().setEndpoints(Consts.API_DOMAIN, Consts.CHAT_DOMAIN, ServiceZone.PRODUCTION);
        QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
    }
}