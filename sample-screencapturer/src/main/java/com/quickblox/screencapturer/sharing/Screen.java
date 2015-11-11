package com.quickblox.screencapturer.sharing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by vadim on 10/21/15.
 */
public class Screen {

    private static final String TAG = Screen.class.getSimpleName();

    private final static int SCREEN_CAPTURER_PERIOD_MS = 200;

    private final CapturingRunnable capturingRunnable =  new CapturingRunnable();

    private final HandlerThread screenThread;
    private final Handler screenThreadHandler;
    private WeakReference<Activity> activityRef;
    private ScreenObserver screenObserver;

    public Screen(Activity activity){
        this.activityRef = new WeakReference<>(activity);
        screenThread = new HandlerThread(TAG);
        screenThread.start();
        screenThreadHandler = new Handler(screenThread.getLooper());
    }

    public void release(){
        screenThread.quit();
        activityRef = null;
    }

    public void setObserver(ScreenObserver screenObserver){
        this.screenObserver = screenObserver;
    }

    public final void startPreview(){
        Log.i(TAG, "startPreview");
        screenThreadHandler.postDelayed(capturingRunnable, SCREEN_CAPTURER_PERIOD_MS);
    }

    public final void stopPreview(){
        Log.i(TAG, "stopPreview");
        screenThreadHandler.removeCallbacks(capturingRunnable);
    }

    private void notifyObserver(Bitmap drawingCache, int rotation) {
        if (screenObserver != null) {
            screenObserver.onPreviewFrame(drawingCache, rotation);
        }
    }

    private Activity getActivity(){
        return activityRef.get();
    }

    Thread getThread() {
        return screenThread;
    }

    public Handler getHandler() {
        return screenThreadHandler;
    }

    public interface ScreenObserver{
        void onPreviewFrame(Bitmap bitmap, int rotation);
    }

    private class CapturingRunnable implements Runnable {

        @Override
        public void run() {
            Activity activity = getActivity();
            if (activity == null) {
                Log.e(TAG, "Lost activity context");
            }
            View decorView = activity.getWindow().getDecorView().getRootView();
            decorView.setDrawingCacheEnabled(true);
            Bitmap drawingCache = decorView.getDrawingCache();
            if (drawingCache != null){
                Log.i(TAG, "drawingCache="+drawingCache.getWidth() + ":"+drawingCache.getHeight());
                notifyObserver(drawingCache, -1);
            }
            screenThreadHandler.postDelayed(this, SCREEN_CAPTURER_PERIOD_MS);
        }
    }

}
