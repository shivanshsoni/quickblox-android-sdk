package com.quickblox.screencapturer.sharing;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenCapturer implements Screen.ScreenObserver {

    private static final String TAG = ScreenCapturer.class.getSimpleName();

    private final static int SCREEN_OBSERVER_PERIOD_MS = 5000;

    private final ScreenCapturerObserver screenObserver = new ScreenCapturerObserver();

    private  HandlerThread screenThread;
    private  Handler screenThreadHandler;
    private FramePool videoBuffers;
    private Context applicationContext;
    private CapturerSimpleObserver frameObserver;
    private int requestedWidth;
    private int requestedHeight;
    private int requestedFramerate;
    private int captureBuffersCount;
    private int cameraFramesCount;
    private CaptureFormat captureFormat;
    private Screen screen;

    public ScreenCapturer(){
        //Logging.d(TAG, "ScreenCapturer");
        screenThread = new HandlerThread(TAG);
        screenThread.start();
        screenThreadHandler = new Handler(screenThread.getLooper());
    }

    // Called by native code.
    //
    // Note that this actually opens the camera, and Camera callbacks run on the
    // thread that calls open(), so this is done on the CameraThread.
    public void startCapture(
            final int width, final int height, final int framerate,
            final Context applicationContext, final CapturerSimpleObserver frameObserver) {
        Log.d(TAG, "startCapture requested: " + width + "x" + height
                + "@" + framerate);
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext not set.");
        }
        if (frameObserver == null) {
            throw new RuntimeException("frameObserver not set.");
        }

         startCaptureOnScreenThread(width, height, framerate, frameObserver,
                        applicationContext);

    }

    // Called by native code.  Returns true when camera is known to be stopped.
    public void stopCapture(){
        Log.d(TAG, "stopCapture");
        stopCaptureOnCameraThread();
        Log.d(TAG, "stopCapture done");
    }

    private void checkIsOnScreenThread() {
        /*if (Thread.currentThread() != screenThread) {
            throw new IllegalStateException("Wrong thread");
        }*/
    }

    private void startCaptureOnScreenThread(
            int width, int height, int framerate, CapturerSimpleObserver frameObserver,
            Context applicationContext) {
        Throwable error = null;
        //checkIsOnScreenThread();
        this.applicationContext = applicationContext;
        this.frameObserver = frameObserver;
        try {
            obtainScreen(applicationContext);
            videoBuffers = new FramePool(screen.getThread());
            startPreviewOnCameraThread(width, height, framerate);
            frameObserver.OnCapturerStarted(true);

            // Start camera observer.
            cameraFramesCount = 0;
            captureBuffersCount = 0;
            screenThreadHandler.postDelayed(screenObserver, SCREEN_OBSERVER_PERIOD_MS);
            return;
        } catch (RuntimeException e) {
            error = e;
        }
        Log.e(TAG, "startCapture failed", error);
        stopCaptureOnCameraThread();
        frameObserver.OnCapturerStarted(false);
    }

    private void obtainScreen(Context applicationContext) {
        screen = new Screen((Activity) applicationContext);
    }



    private void stopCaptureOnCameraThread() {
        screen.stopPreview();
        screenThreadHandler.removeCallbacks(screenObserver);
        videoBuffers.stopReturnBuffersToCamera();
        screen.release();
        screen = null;
    }

    // (Re)start preview with the closest supported format to |width| x |height| @ |framerate|.
    private void startPreviewOnCameraThread(int width, int height, int framerate) {
        checkIsOnScreenThread();

        requestedWidth = width;
        requestedHeight = height;
        requestedFramerate = framerate;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)applicationContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        CaptureFormat captureFormat = new CaptureFormat(
                displayMetrics.widthPixels, displayMetrics.heightPixels,
                30, 60, ((Activity)applicationContext).getWindow().getDecorView().isOpaque());

        // (Re)start preview.
        Log.d(TAG, "Start capturing: " + captureFormat);
        this.captureFormat = captureFormat;
        videoBuffers.queueCameraBuffers(captureFormat.frameSize());

        startPreview();
    }

    private void stopPreview() {

    }

    private void startPreview() {
        screen.setObserver(this);
        screen.startPreview();
    }


   /* // Called on cameraThread so must not "synchronized".
    public void onPreviewFrame(byte[] data, Camera callbackCamera) {
        checkIsOnScreenThread();

        final long captureTimeNs =
                TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

        captureBuffersCount += videoBuffers.numCaptureBuffersAvailable();
        int rotation = getDeviceOrientation();
        // Mark the frame owning |data| as used.
        // Note that since data is directBuffer,
        // data.length >= videoBuffers.frameSize.
        if (videoBuffers.reserveByteBuffer(data, captureTimeNs)) {
            cameraFramesCount++;
            frameObserver.OnFrameCaptured(data, videoBuffers.frameSize, captureFormat.width,
                    captureFormat.height, rotation, captureTimeNs);
        } else {
            Log.w(TAG, "reserveByteBuffer failed - dropping frame.");
        }
    }*/

    private int getDeviceOrientation() {
        return ((Activity)applicationContext).getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void onPreviewFrame(Bitmap bitmap, int rotation) {

        final long captureTimeNs =
                TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        Log.i(TAG, "onPreviewFrame time="+captureTimeNs);
        Log.i(TAG, "onPreviewFrame bmp width="+bitmap.getWidth() +", hight="+bitmap.getHeight()
                +", bytes count="+bitmap.getByteCount());
        captureBuffersCount = videoBuffers.numCaptureBuffersAvailable();
        int currRotation = getDeviceOrientation();
        // Mark the frame owning |data| as used.
        // Note that since data is directBuffer,
        // data.length >= videoBuffers.frameSize.
        // Mark the frame owning |data| as used.
        // Note that since data is directBuffer,
        // data.length >= videoBuffers.frameSize.
        byte[] bytes = convertBmpToBytes(bitmap);
        if (videoBuffers.reserveByteBuffer(bytes, captureTimeNs)) {
            cameraFramesCount++;
            frameObserver.OnFrameCaptured(bitmap, rotation, captureTimeNs);
        } else {
            Log.w(TAG, "reserveByteBuffer failed - dropping frame.");
        }
    }

    // Camera observer - monitors camera framerate and amount of available
    // camera buffers. Observer is excecuted on camera thread.
    private class ScreenCapturerObserver implements Runnable {
        @Override
        public void run() {
            int cameraFps = (cameraFramesCount * 1000 + SCREEN_OBSERVER_PERIOD_MS / 2)
                    / SCREEN_OBSERVER_PERIOD_MS;
            double averageCaptureBuffersCount = 0;
            if (cameraFramesCount > 0) {
                averageCaptureBuffersCount =
                        (double)captureBuffersCount / cameraFramesCount;
            }
            Log.d(TAG, "Camera fps: " + cameraFps + ". CaptureBuffers: " +
                    String.format("%.1f", averageCaptureBuffersCount) +
                    ". Pending buffers: " + videoBuffers.pendingFramesTimeStamps());
            if (cameraFramesCount == 0) {
                Log.e(TAG, "Camera freezed.");
            } else {
                cameraFramesCount = 0;
                captureBuffersCount = 0;
                screenThreadHandler.postDelayed(this, SCREEN_OBSERVER_PERIOD_MS);
            }
        }
    };


    private byte[] convertBmpToBytes(Bitmap bmp){

        int bytes = byteSizeOf(bmp);

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        bmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        return buffer.array(); //Get the underlying array containing the data.

    }

    protected int byteSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        }
        else {
            return data.getAllocationByteCount();
        }
    }

    // Class used for allocating and bookkeeping video frames. All buffers are
    // direct allocated so that they can be directly used from native code. This class is
    // not thread-safe, and enforces single thread use.
    private static class FramePool {
        // Thread that all calls should be made on.
        private final Thread thread;
        // Arbitrary queue depth.  Higher number means more memory allocated & held,
        // lower number means more sensitivity to processing time in the client (and
        // potentially stalling the capturer if it runs out of buffers to write to).
        private static final int numCaptureBuffers = 1;
        // This container tracks the buffers added as camera callback buffers. It is needed for finding
        // the corresponding ByteBuffer given a byte[].
        private final Map<Integer, ByteBuffer> queuedBuffers = new HashMap<>();
        // This container tracks the frames that have been sent but not returned. It is needed for
        // keeping the buffers alive and for finding the corresponding ByteBuffer given a timestamp.
        private final Map<Long, ByteBuffer> pendingBuffers = new HashMap<>();
        private int frameSize = 0;
        private Camera camera;

        public FramePool(Thread thread) {
            this.thread = thread;
        }

        private void checkIsOnValidThread() {
            /*if (Thread.currentThread() != thread) {
                throw new IllegalStateException("Wrong thread");
            }*/
        }

        public int numCaptureBuffersAvailable() {
            checkIsOnValidThread();
            return queuedBuffers.size();
        }

        // Discards previous queued buffers and adds new callback buffers to camera.
        public void queueCameraBuffers(int frameSize) {
            checkIsOnValidThread();
            this.frameSize = frameSize;

            queuedBuffers.clear();
            for (int i = 0; i < numCaptureBuffers; ++i) {
                final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
                queuedBuffers.put(frameSize, buffer);
            }
            Log.d(TAG, "queueCameraBuffers enqueued " + numCaptureBuffers
                    + " buffers of size " + frameSize + ".");
        }

        // Return number of pending frames that have not been returned.
        public int pendingFramesCount() {
            checkIsOnValidThread();
            return pendingBuffers.size();
        }

        public String pendingFramesTimeStamps() {
            checkIsOnValidThread();
            List<Long> timeStampsMs = new ArrayList<Long>();
            for (Long timeStampNs : pendingBuffers.keySet()) {
                timeStampsMs.add(TimeUnit.NANOSECONDS.toMillis(timeStampNs));
            }
            return timeStampsMs.toString();
        }

        public void stopReturnBuffersToCamera() {
            checkIsOnValidThread();
            this.camera = null;
            queuedBuffers.clear();
            // Frames in |pendingBuffers| need to be kept alive until they are returned.
            Log.d(TAG, "stopReturnBuffersToCamera called."
                    + (pendingBuffers.isEmpty() ?
                    " All buffers have been returned."
                    : " Pending buffers: " + pendingFramesTimeStamps() + "."));
        }

        public boolean reserveByteBuffer(byte[] data, long timeStamp) {
            checkIsOnValidThread();
            final ByteBuffer buffer = queuedBuffers.remove(data.length);
            if (buffer == null) {
                // Frames might be posted to |onPreviewFrame| with the previous format while changing
                // capture format in |startPreviewOnCameraThread|. Drop these old frames.
                Log.w(TAG, "Received callback buffer from previous configuration with length: "
                        + (data == null ? "null" : data.length));
                return false;
            }
            if (buffer.capacity() != frameSize) {
                throw new IllegalStateException("Callback buffer has unexpected frame size");
            }
            if (pendingBuffers.containsKey(timeStamp)) {
                Log.e(TAG, "Timestamp already present in pending buffers - they need to be unique");
                return false;
            }
            pendingBuffers.put(timeStamp, buffer);
            if (queuedBuffers.isEmpty()) {
                Log.v(TAG, "Camera is running out of capture buffers."
                        + " Pending buffers: " + pendingFramesTimeStamps());
            }
            return true;
        }

        public void returnBuffer(long timeStamp) {
            checkIsOnValidThread();
            final ByteBuffer returnedFrame = pendingBuffers.remove(timeStamp);
            if (returnedFrame == null) {
                throw new RuntimeException("unknown data buffer with time stamp "
                        + timeStamp + "returned?!?");
            }

            if (camera != null && returnedFrame.capacity() == frameSize) {
                camera.addCallbackBuffer(returnedFrame.array());
                if (queuedBuffers.isEmpty()) {
                    Log.v(TAG, "Frame returned when camera is running out of capture"
                            + " buffers for TS " + TimeUnit.NANOSECONDS.toMillis(timeStamp));
                }
                queuedBuffers.put(returnedFrame.array().length, returnedFrame);
                return;
            }

            if (returnedFrame.capacity() != frameSize) {
                Log.d(TAG, "returnBuffer with time stamp "
                        + TimeUnit.NANOSECONDS.toMillis(timeStamp)
                        + " called with old frame size, " + returnedFrame.capacity() + ".");
                // Since this frame has the wrong size, don't requeue it. Frames with the correct size are
                // created in queueCameraBuffers so this must be an old buffer.
                return;
            }

            Log.d(TAG, "returnBuffer with time stamp "
                    + TimeUnit.NANOSECONDS.toMillis(timeStamp)
                    + " called after camera has been stopped.");
        }
    }

    // Interface used for providing callbacks to an observer.
    interface CapturerObserver {
        // Notify if the camera have been started successfully or not.
        // Called on a Java thread owned by VideoCapturerAndroid.
        abstract void OnCapturerStarted(boolean success);

        // Delivers a captured frame. Called on a Java thread owned by
        // VideoCapturerAndroid.
        abstract void OnFrameCaptured(byte[] data, int length, int width, int height,
                                      int rotation, long timeStamp);

        // Requests an output format from the video capturer. Captured frames
        // by the camera will be scaled/or dropped by the video capturer.
        // Called on a Java thread owned by VideoCapturerAndroid.
        abstract void OnOutputFormatRequest(int width, int height, int fps);
    }

    // Interface used for providing callbacks to an observer.
    public interface CapturerSimpleObserver {

         void OnCapturerStarted(boolean success);

         void OnFrameCaptured(Bitmap bmp,
                                      int rotation, long timeStamp);

         void OnFrameCaptured(int[] data, int width, int height,
                             int rotation, long timeStamp);

         void OnOutputFormatRequest(int width, int height, int fps);
    }

    // An implementation of CapturerObserver that forwards all calls from
    // Java to the C layer.
   /* static class NativeObserver implements CapturerSimpleObserver {
        private final long nativeCapturer;

        public NativeObserver(long nativeCapturer) {
            this.nativeCapturer = nativeCapturer;
        }

        @Override
        public void OnCapturerStarted(boolean success) {
            nativeCapturerStarted(nativeCapturer, success);
        }

        @Override
        public void OnFrameCaptured(Bitmap bmp, int rotation, long timeStamp) {

        }

        @Override
        public void OnFrameCaptured(int[] data, int rotation, long timeStamp) {
            nativeOnFrameCaptured(nativeCapturer, data, rotation, timeStamp);
        }

        @Override
        public void OnOutputFormatRequest(int width, int height, int fps) {
            nativeOnOutputFormatRequest(nativeCapturer, width, height, fps);
        }

        private native void nativeCapturerStarted(long nativeCapturer,
                                                  boolean success);
        private native void nativeOnFrameCaptured(long nativeCapturer,
                                                  byte[] data, int rotation, long timeStamp);
        private native void nativeOnOutputFormatRequest(long nativeCapturer,
                                                        int width, int height, int fps);
    }*/
}
