package com.quickblox.screencapturer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.screencapturer.util.BinaryInstallerUtil;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BinaryInstallerUtil binaryInstallerUtil = new BinaryInstallerUtil(this);
        binaryInstallerUtil.installBinary();
        binaryInstallerUtil.runBinary();
    }
}