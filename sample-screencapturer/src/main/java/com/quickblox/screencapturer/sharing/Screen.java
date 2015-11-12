package com.quickblox.screencapturer.sharing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.quickblox.screencapturer.IScreenshotProvider;

/**
 * Created by vadim on 10/21/15.
 */
public class Screen {

    private static final String TAG = Screen.class.getSimpleName();

    private final static int SCREEN_CAPTURER_PERIOD_MS = 200;

    private final CapturingRunnable capturingRunnable = new CapturingRunnable();

    private final HandlerThread screenThread;
    private final Handler screenThreadHandler;
    private ScreenObserver screenObserver;
    private IScreenshotProvider screenshotProvider;

    public Screen(IScreenshotProvider screenshotProvider) {
        this.screenshotProvider = screenshotProvider;
        screenThread = new HandlerThread(TAG);
        screenThread.start();
        screenThreadHandler = new Handler(screenThread.getLooper());
    }

    public void release() {
        screenThread.quit();
    }

    public void setObserver(ScreenObserver screenObserver) {
        this.screenObserver = screenObserver;
    }

    public final void startPreview() {
        Log.i(TAG, "startPreview");
        screenThreadHandler.postDelayed(capturingRunnable, SCREEN_CAPTURER_PERIOD_MS);
    }

    public final void stopPreview() {
        Log.i(TAG, "stopPreview");
        screenThreadHandler.removeCallbacks(capturingRunnable);
    }

    private void notifyObserver(Bitmap drawingCache, int rotation) {
        if (screenObserver != null) {
            screenObserver.onPreviewFrame(drawingCache, rotation);
        }
    }

    Thread getThread() {
        return screenThread;
    }

    public Handler getHandler() {
        return screenThreadHandler;
    }

    public interface ScreenObserver {
        void onPreviewFrame(Bitmap bitmap, int rotation);
    }

    private class CapturingRunnable implements Runnable {

        @Override
        public void run() {
            try {
                String file = screenshotProvider.takeScreenshot();
                Bitmap drawingCache = BitmapFactory.decodeFile(file);
                if (drawingCache != null) {
                    Log.i(TAG, "drawingCache=" + drawingCache.getWidth() + ":" + drawingCache.getHeight());
                    notifyObserver(drawingCache, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            screenThreadHandler.postDelayed(this, SCREEN_CAPTURER_PERIOD_MS);
        }
    }

}
