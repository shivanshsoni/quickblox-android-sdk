package com.quickblox.screencapturer.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.quickblox.screencapturer.IScreenshotProvider;
import com.quickblox.screencapturer.R;
import com.quickblox.screencapturer.ScreenshotService;
import com.quickblox.screencapturer.sharing.ScreenCapturer;


public class ScreenCaptiringFragment extends Fragment implements View.OnClickListener, ScreenCapturer.CapturerSimpleObserver {

    private static final String TAG = ScreenCaptiringFragment.class.getSimpleName();
    private ImageView imageView;
    private ScreenCapturer screenCapturer;
    private DisplayMetrics displayMetrics;

    private IScreenshotProvider screenshotProvider = null;
    private ServiceConnection aslServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            screenshotProvider = IScreenshotProvider.Stub.asInterface(binder);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            screenshotProvider = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.screen_capturing_fargment, container, false);
        initUi(rootView);
        return rootView;
    }

    private void initUi(View rootView) {
        imageView = (ImageView) rootView.findViewById(R.id.iamge);
        rootView.findViewById(R.id.startPreview).setOnClickListener(this);
        rootView.findViewById(R.id.stopPreview).setOnClickListener(this);
        screenCapturer = new ScreenCapturer();
        displayMetrics = new DisplayMetrics();
        (getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Intent intent = new Intent();
        intent.setClass(context, ScreenshotService.class);
        context.bindService(intent, aslServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startPreview :
                screenCapturer.startCapture(displayMetrics.widthPixels, displayMetrics.heightPixels,
                        30, getActivity(), this, screenshotProvider);
                break;
            case R.id.stopPreview :
                screenCapturer.stopCapture();
                break;
            default:break;
        }
    }

    @Override
    public void OnCapturerStarted(boolean success) {
        Toast.makeText(getActivity(), "Started capturing", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnFrameCaptured(final Bitmap bmp, final int rotation, long timeStamp) {
        Log.i(TAG, "OnFrameCaptured");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageDrawable(new BitmapDrawable(getResources(), bmp));
            }
        });
    }

    @Override
    public void OnFrameCaptured(int[] data, int width, int height, int rotation, long timeStamp) {

    }

    @Override
    public void OnOutputFormatRequest(int width, int height, int fps) {

    }
}
