package com.quickblox.sample.videochatwebrtcnew.sharing;

import android.graphics.ImageFormat;

import static java.lang.Math.ceil;

/**
 * Created by vadim on 10/21/15.
 */
public class CaptureFormat {

    public final int width;
    public final int height;
    public final int maxFramerate;
    public final int minFramerate;
    // TODO(hbos): If VideoCapturerAndroid.startCapture is updated to support
    // other image formats then this needs to be updated and
    // VideoCapturerAndroid.getSupportedFormats need to return CaptureFormats of
    // all imageFormats.
    public final int imageFormat = ImageFormat.YV12;

    public CaptureFormat(int width, int height, int minFramerate,
                         int maxFramerate) {
        this.width = width;
        this.height = height;
        this.minFramerate = minFramerate;
        this.maxFramerate = maxFramerate;
    }

    // Calculates the frame size of this capture format.
    public int frameSize() {
        return frameSize(width, height, imageFormat);
    }

    // Calculates the frame size of the specified image format. Currently only
    // supporting ImageFormat.YV12. The YV12's stride is the closest rounded up
    // multiple of 16 of the width and width and height are always even.
    // Android guarantees this:
    // http://developer.android.com/reference/android/hardware/Camera.Parameters.html#setPreviewFormat%28int%29
    public static int frameSize(int width, int height, int imageFormat) {
        if (imageFormat != ImageFormat.YV12) {
            throw new UnsupportedOperationException("Don't know how to calculate "
                    + "the frame size of non-YV12 image formats.");
        }
        int yStride = roundUp(width, 16);
        int uvStride = roundUp(yStride / 2, 16);
        int ySize = yStride * height;
        int uvSize = uvStride * height / 2;
        return ySize + uvSize * 2;
    }

    // Rounds up |x| to the closest value that is a multiple of |alignment|.
    private static int roundUp(int x, int alignment) {
        return (int)ceil(x / (double)alignment) * alignment;
    }

    @Override
    public String toString() {
        return width + "x" + height + "@[" + minFramerate + ":" + maxFramerate + "]";
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof CaptureFormat)) {
            return false;
        }
        final CaptureFormat c = (CaptureFormat) that;
        return width == c.width && height == c.height && maxFramerate == c.maxFramerate
                && minFramerate == c.minFramerate;
    }
}
