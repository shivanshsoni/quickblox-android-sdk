package com.quickblox.sample.videochatwebrtcnew.camera;

import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;

import com.quickblox.sample.videochatwebrtcnew.sharing.CaptureFormat;

import java.util.ArrayList;
import java.util.List;


public class CameraHelper {

    private final static String TAG = CameraHelper.class.getSimpleName();
    // Each entry contains the supported formats for corresponding camera index. The formats for all
    // cameras are enumerated on the first call to getSupportedFormats(), and cached for future
    // reference.
    private static List<List<CaptureFormat>> cachedSupportedFormats;

    public static List<CaptureFormat> getSupportedFormats(int cameraId) {
        synchronized (CameraHelper.class) {
            if (cachedSupportedFormats == null) {
                cachedSupportedFormats = new ArrayList<List<CaptureFormat>>();
                for (int i = 0; i < getDeviceCount(); ++i) {
                    cachedSupportedFormats.add(enumerateFormats(i));
                }
            }
        }
        return cachedSupportedFormats.get(cameraId);
    }

    public static int getIdOfFrontFacingDevice() {
        for(int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();

            try {
                Camera.getCameraInfo(i, info);
                if(info.facing == 1) {
                    return i;
                }
            } catch (Exception var3) {
                Log.e(TAG, "getCameraInfo failed on index " + i, var3);
            }
        }

        return -1;
    }

    private static List<CaptureFormat> enumerateFormats(int cameraId) {
        Log.d(TAG, "Get supported formats for camera index " + cameraId + ".");
        final long startTimeMs = SystemClock.elapsedRealtime();
        final Camera.Parameters parameters;
        Camera camera = null;
        try {
            Log.d(TAG, "Opening camera with index " + cameraId);
            camera = Camera.open(cameraId);
            parameters = camera.getParameters();
        } catch (RuntimeException e) {
            Log.e(TAG, "Open camera failed on camera index " + cameraId, e);
            return new ArrayList<CaptureFormat>();
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        final List<CaptureFormat> formatList = new ArrayList<CaptureFormat>();
        try {
            int minFps = 0;
            int maxFps = 0;
            final List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
            if (listFpsRange != null) {
                // getSupportedPreviewFpsRange() returns a sorted list. Take the fps range
                // corresponding to the highest fps.
                final int[] range = listFpsRange.get(listFpsRange.size() - 1);
                minFps = range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                maxFps = range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            }
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                formatList.add(new CaptureFormat(size.width, size.height, minFps, maxFps));
            }
        } catch (Exception e) {
            Log.e(TAG, "getSupportedFormats() failed on camera index " + cameraId, e);
        }
        final long endTimeMs = SystemClock.elapsedRealtime();
        Log.d(TAG, "Get supported formats for camera index " + cameraId + " done."
                + " Time spent: " + (endTimeMs - startTimeMs) + " ms.");
        return formatList;
    }


    // Returns number of cameras on device.
    public static int getDeviceCount() {
        return Camera.getNumberOfCameras();
    }
}
