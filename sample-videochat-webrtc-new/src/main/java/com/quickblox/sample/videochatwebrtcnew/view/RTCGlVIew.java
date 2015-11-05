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

    public VideoRenderer.Callbacks obtainVideoRenderer(RendererSurface rendererSurface){
        Log.i(TAG, "obtainVideoRenderer");

        return RendererSurface.MAIN.equals(rendererSurface) ? obtainMainVideoRenderer() :
                obtainSecondVideoRenderer() ;
    }

    private VideoRenderer.Callbacks obtainMainVideoRenderer(){
        Log.i(TAG, "obtainMainVideoRenderer");
        if (mainRendererCallback == null) {
            mainRendererCallback = initRenderer(mainMirror, remoteCoords);
        }
        return mainRendererCallback;
    }

    private VideoRenderer.Callbacks obtainSecondVideoRenderer(){
        Log.i(TAG, "obtainSecondVideoRenderer");
        if (localRendererCallback == null) {
            localRendererCallback = initRenderer(secondMirror, localCoords);
        }
        return localRendererCallback;
    }

    public void updateRenderer(RendererSurface rendererSurface, RendererConfig config){
        Log.i(TAG, "updateRenderer viewType=" + viewType);
        boolean mainRenderer = RendererSurface.MAIN.equals(rendererSurface);
        VideoRenderer.Callbacks callbacks = mainRenderer ? mainRendererCallback
                :localRendererCallback;

        if (config.coordinates != null) {
            setViewCoordinates((mainRenderer ? remoteCoords : localCoords),
                    config.coordinates);
        }
        setRendererMirror(config.mirror, rendererSurface);
        int[] viewCoordinates = mainRenderer ? remoteCoords : localCoords;
        VideoRendererGui.update(callbacks, viewCoordinates[0], viewCoordinates[1],
                viewCoordinates[2], viewCoordinates[3],
                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL,
                (mainRenderer ? mainMirror : secondMirror));
    }

    public void  release(){
        if (localRendererCallback != null) {
            VideoRendererGui.remove(localRendererCallback);
        }
        if (mainRendererCallback != null) {
            VideoRendererGui.remove(mainRendererCallback);
        }
    }

    private void setRendererMirror(boolean mirror, RendererSurface type){
        Log.i(TAG, "setRendererMirror type="+type +", value= "+mirror);
        if (RendererSurface.MAIN.equals(type)){
            mainMirror = mirror;
        } else {
            secondMirror = mirror;
        }
    }

    private VideoRenderer.Callbacks initRenderer(boolean mirror, int[] viewCoordinates) {
        return VideoRendererGui.createGuiRenderer(
                    viewCoordinates[0], viewCoordinates[1],
                    viewCoordinates[2], viewCoordinates[3],
                    VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, mirror);

    }

    private void init(TypedArray typedArray) {
        VideoRendererGui.setView(this, null);
        if (typedArray != null) {
            setValuefromResources(typedArray);
            typedArray.recycle();
        }

        obtainMainVideoRenderer();
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

        setRendererMirror(typedArray.getBoolean(R.styleable.RTCGlView_mainMirror, false),
                RendererSurface.MAIN);
        setRendererMirror(typedArray.getBoolean(R.styleable.RTCGlView_secondMirror, false),
                RendererSurface.SECOND);

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

    public static class RendererConfig{
        public int[] coordinates;
        public boolean mirror;
    }

    public enum RendererSurface {
        MAIN, SECOND
    }

    public enum ViewType {
        LOCAL, REMOTE, BOTH;
    }

}

