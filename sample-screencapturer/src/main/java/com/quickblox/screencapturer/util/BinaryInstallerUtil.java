package com.quickblox.screencapturer.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryInstallerUtil {

    public static final String SOX_NAME_ARM = "asl-native";

    private Context context;

    public BinaryInstallerUtil(Context context) {
        this.context = context.getApplicationContext();
    }

    public void installBinary() {
        try {
            writeSoxToDataFolder();
            setExecRights();
        } catch (Exception e) {
            Log.e(BinaryInstallerUtil.class.getSimpleName(), "Can't install binary" ,e);
        }
    }

    public String pathToSox() {
        return context.getFilesDir().getAbsolutePath() + "/" + SOX_NAME_ARM;
    }

    private void writeSoxToDataFolder() throws IOException {
        String soxName = SOX_NAME_ARM;
        InputStream binaryInputStream = context.getAssets().open(soxName);
        OutputStream binaryOutputStream = context.openFileOutput(SOX_NAME_ARM, Context.MODE_PRIVATE);

        byte[] buffer = new byte[4096];
        while (binaryInputStream.read(buffer) > 0) {
            binaryOutputStream.write(buffer);
        }

        binaryOutputStream.close();
        binaryInputStream.close();
    }

    private void setExecRights() throws InterruptedException, IOException {
        String commandToExec = "chmod 700" + " " + pathToSox();
        Log.d(BinaryInstallerUtil.class.getSimpleName(), commandToExec);

        Process process = Runtime.getRuntime().exec(commandToExec);
        process.waitFor();
    }
}
