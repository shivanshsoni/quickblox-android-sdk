package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.sharing.ScreenCapturer;


/**
 * Created by vadim on 10/21/15.
 */
public class ScreenCaptiringFragment extends Fragment implements View.OnClickListener, ScreenCapturer.CapturerSimpleObserver {

    private static final String TAG = ScreenCaptiringFragment.class.getSimpleName();
    private ImageView imageView;
    private ScreenCapturer screenCapturer;
    private DisplayMetrics displayMetrics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.screen_capturing_fargment, null);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startPreview :
                screenCapturer.startCapture(displayMetrics.widthPixels, displayMetrics.heightPixels,
                        30, getActivity(), this);
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
    public void OnFrameCaptured(int[] data, int rotation, long timeStamp) {

    }

    @Override
    public void OnOutputFormatRequest(int width, int height, int fps) {

    }
}
