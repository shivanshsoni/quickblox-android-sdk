package com.quickblox.sample.videochatwebrtcnew.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.quickblox.sample.videochatwebrtcnew.R;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;


public class RTCGlVIew extends GLSurfaceView{


    private static final String TAG = RTCGlVIew.class.getSimpleName();
    private static final int NUMBER_COORDINATES = 4;
    private VideoRenderer.Callbacks mainRendererCallback;
    private VideoRenderer.Callbacks localRendererCallback;

    private final int[] remoteCoords = {0, 0, 100, 100};
    private final int[] localCoords = {0, 0, 100, 100};
    private ViewType viewType = ViewType.BOTH;
    private boolean mainMirror;
    private boolean secondMirror;

    public RTCGlVIew(Context context) {
        super(context);
        Log.i(TAG, "ctor");
        init(null);
    }

    public RTCGlVIew(Context c, AttributeSet attr) {
        super(c, attr);
        Log.i(TAG, "ctor with attrs");
        TypedArray a = c.getTheme().obtainStyledAttributes(
                attr,
                R.styleable.RTCGlView,
                0, 0);
        init(a);
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public void setRemoteViewCoordinates(int[] values){
        setViewCoordinates(remoteCoords, values);
    }

    public void setLocalViewCoordinates(int[] values){
        setViewCoordinates(localCoords, values);
    }

    public VideoRenderer.Callbacks obtainMainVideoRenderer(){
        Log.i(TAG, "obtainMainVideoRenderer");
        return mainRendererCallback;
    }

    public VideoRenderer.Callbacks obtainSecondVideoRenderer(){
        Log.i(TAG, "obtainSecondVideoRenderer");
        localRendererCallback = initRenderer(secondMirror, localCoords);
        return localRendererCallback;
    }

    private VideoRenderer.Callbacks initRenderer(boolean local, int[] viewCoordinates) {
        return VideoRendererGui.createGuiRenderer(
                    viewCoordinates[0], viewCoordinates[1],
                    viewCoordinates[2], viewCoordinates[3],
                    VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, local);

    }

    private void init(TypedArray typedArray) {
        VideoRendererGui.setView(this, null);
        if (typedArray != null) {
            setValuefromResources(typedArray);
            typedArray.recycle();
        }

        mainRendererCallback = initRenderer(mainMirror, remoteCoords);
    }

    private void setValuefromResources(TypedArray typedArray){

        Log.i(TAG, "setValuefromResources");
        int viewType = typedArray.getInt(R.styleable.RTCGlView_viewType, -1);
        if (viewType >= 0){
            Log.i(TAG, "view type="+viewType);
            for (ViewType type :ViewType.values()){
                if (type.ordinal() == viewType){
                    Log.i(TAG, "view enum="+type.toString());
                    setViewType(type);
                    break;
                }
            }
        }

        mainMirror = typedArray.getBoolean(R.styleable.RTCGlView_mainMirror, false);
        secondMirror = typedArray.getBoolean(R.styleable.RTCGlView_secondMirror, false);

        final int remoteValuesId = typedArray.getResourceId(R.styleable.RTCGlView_firstCoords, 0);

        if (remoteValuesId != 0) {
            int[] values = getResources().getIntArray(remoteValuesId);
            setViewCoordinates(remoteCoords, values);
        }

        final int localValuesId = typedArray.getResourceId(R.styleable.RTCGlView_secondCoords, 0);
        if (localValuesId != 0) {
            int[] values = getResources().getIntArray(localValuesId);
            setViewCoordinates(localCoords, values);
        }
    }

    private void setViewCoordinates(int[] coordinates, int[] resources){
        if (resources.length >= NUMBER_COORDINATES) {
            System.arraycopy(resources, 0, coordinates, 0, NUMBER_COORDINATES);
        }
    }

    public enum ViewType {
        LOCAL, REMOTE, BOTH;
    }

}

