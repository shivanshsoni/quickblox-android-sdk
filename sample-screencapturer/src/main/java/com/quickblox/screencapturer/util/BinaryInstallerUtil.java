package com.quickblox.screencapturer.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryInstallerUtil {

    public static final String BINARY_FILENAME = "asl-native";

    private Context context;

    public BinaryInstallerUtil(Context context) {
        this.context = context.getApplicationContext();
    }

    public void installBinary() {
        try {
            writeBinaryToDataFolder();
            setExecRights();
        } catch (Exception e) {
            Log.e(BinaryInstallerUtil.class.getSimpleName(), "Can't install binary", e);
        }
    }

    public void runBinary() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String commandToExec = pathToBinary();
                    Process process = Runtime.getRuntime().exec(commandToExec);
                    process.waitFor();
                } catch (Exception e) {
                    Log.e(BinaryInstallerUtil.class.getSimpleName(), "Can't run binary", e);
                }
            }
        });
    }

    public String pathToBinary() {
        return context.getFilesDir().getAbsolutePath() + "/" + BINARY_FILENAME;
    }

    private void writeBinaryToDataFolder() throws IOException {
        InputStream binaryInputStream = context.getAssets().open(BINARY_FILENAME);
        OutputStream binaryOutputStream = context.openFileOutput(BINARY_FILENAME, Context.MODE_PRIVATE);

        byte[] buffer = new byte[4096];
        while (binaryInputStream.read(buffer) > 0) {
            binaryOutputStream.write(buffer);
        }

        binaryOutputStream.close();
        binaryInputStream.close();
    }

    private void setExecRights() throws InterruptedException, IOException {
        String commandToExec = "chmod 700" + " " + pathToBinary();
        Log.d(BinaryInstallerUtil.class.getSimpleName(), commandToExec);

        Process process = Runtime.getRuntime().exec(commandToExec);
        process.waitFor();
    }
}
