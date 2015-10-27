package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.sample.videochatwebrtcnew.view.RTCGlVIew;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment implements Serializable, QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, CallActivity.QBRTCSessionUserCallback {

    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;

    private String TAG = ConversationFragment.class.getSimpleName();
    private ArrayList<User> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;

    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private ImageView imgMyCameraOff;
    private TextView incUserName;
    private View view;
    private Map<String, String> userInfo;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private LinearLayout actionVideoButtonsLayout;
    private View actionBar;
    private String callerName;
    private LinearLayout noVideoImageContainer;
    private boolean isMessageProcessed;
    private MediaPlayer ringtone;
    private RTCGlVIew localVideoView;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private CameraState cameraState = CameraState.NONE;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;

    public static ConversationFragment newInstance(List<User> opponents, String callerName,
                        QBRTCTypes.QBConferenceType qbConferenceType,
                                                   Map<String, String> userInfo,  CallActivity.StartConversetionReason reason,
                                                   String sesionnId){

        ConversationFragment fragment = new ConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ApplicationSingleton.CONFERENCE_TYPE, qbConferenceType.getValue());
        bundle.putString(CALLER_NAME, callerName);
        bundle.putSerializable(ApplicationSingleton.OPPONENTS, (Serializable) opponents);
        if (userInfo != null) {
            for (String key : userInfo.keySet()) {
                bundle.putString("UserInfo:" + key, userInfo.get(key));
            }
        }
        bundle.putInt(START_CONVERSATION_REASON, reason.ordinal());
        if (sesionnId != null) {
            bundle.putString(SESSION_ID, sesionnId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        ((CallActivity) getActivity()).initActionBarWithTimer();

        if (getArguments() != null) {
            opponents = (ArrayList<User>) getArguments().getSerializable(ApplicationSingleton.OPPONENTS);
            qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);
            startReason = getArguments().getInt(CallActivity.START_CONVERSATION_REASON);
            sessionID = getArguments().getString(CallActivity.SESSION_ID);
            callerName = getArguments().getString(CallActivity.CALLER_NAME);

            Log.d(TAG, "CALLER_NAME: " + callerName);
            Log.d(TAG, "opponents: " + opponents.toString());
        }

        initViews(view);
        initButtonsListener();
        initSessionListener();
//        createOpponentsList(opponents);
        setUpUiByCallType(qbConferenceType);

        return view;

    }

    private void initSessionListener() {
        ((CallActivity) getActivity()).addVideoTrackCallbacksListener(this);
    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.getValue()) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);

            localVideoView.setVisibility(View.INVISIBLE);

            imgMyCameraOff.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {


        cameraToggle.setEnabled(enability);
        switchCameraToggle.setEnabled(enability);
        imgMyCameraOff.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);


        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        switchCameraToggle.setActivated(enability);
        imgMyCameraOff.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);
    }


    @Override
    public void onStart() {

        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        QBRTCSession session = ((CallActivity) getActivity()).getCurrentSession();
        if (!isMessageProcessed) {
            if (startReason == CallActivity.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()) {
                session.acceptCall(session.getUserInfo());
            } else {
                session.startCall(session.getUserInfo());
            }
            isMessageProcessed = true;
        }
        ((CallActivity) getActivity()).addTCClientConnectionCallback(this);
        ((CallActivity) getActivity()).addRTCSessionUserCallback(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
    }

    private void initViews(View view) {

        isPeerToPeerCall = opponents.size() == 1;
        localVideoView = (RTCGlVIew) view.findViewById(R.id.localVideoVidew);
        localVideoView.setVisibility(isPeerToPeerCall ? View.INVISIBLE: View.VISIBLE);
        recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
        recyclerView.setHasFixedSize(true);
        final int columnsCount = defineColumnsCount();
        final int rowsCount = defineRowCount();
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnsCount));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(TAG, "onGlobalLayout");
                setGrid(columnsCount, rowsCount);
                recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });


        opponentViewHolders = new SparseArray<>(opponents.size());

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        incUserName = (TextView) view.findViewById(R.id.incUserName);
        incUserName.setText(callerName);
        incUserName.setBackgroundResource(ListUsersActivity.selectBackgrounForOpponent((
                DataHolder.getUserIndexByFullName(callerName)) + 1));

        noVideoImageContainer = (LinearLayout) view.findViewById(R.id.noVideoImageContainer);
        imgMyCameraOff = (ImageView) view.findViewById(R.id.imgMyCameraOff);

        actionButtonsEnabled(false);
    }

    private void setGrid(int columnsCount, int rowsCount){
        int gridWidth = recyclerView.getMeasuredWidth();
        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
        int cellSize = defineMinSize(gridWidth, recyclerView.getMeasuredHeight(),
                columnsCount, rowsCount, itemMargin);
        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSize);
        recyclerView.setAdapter(new OpponentsFromCallAdapter(getActivity(), opponents, cellSize,
                cellSize, gridWidth, columnsCount, (int) itemMargin,
                QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue() == qbConferenceType
        ));
    }

    private int defineMinSize(int measuredWidth, int measuredHeight, int columnsCount, int rowsCount, float padding) {
        int cellWidth = measuredWidth/columnsCount - (int)(padding * 2);
        int cellHeight = measuredHeight/rowsCount - (int)(padding * 2);
        return Math.min(cellWidth, cellHeight);
    }

    private int defineRowCount() {
        int result = DEFAULT_ROWS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount < 3) {
            result = opponentsCount;
        }
        return result;

    }

    private int defineColumnsCount() {
        int result = DEFAULT_COLS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount == 1 || opponentsCount == 2) {
            result = 1;
        } else if (opponentsCount == 4){
            result = 2;
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue()) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(audioStreamReceiver);
        ((CallActivity) getActivity()).removeRTCClientConnectionCallback(this);
        ((CallActivity) getActivity()).removeRTCSessionUserCallback(this);
    }

    private void initButtonsListener() {

        switchCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    ((CallActivity) getActivity()).getCurrentSession().switchCapturePosition(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(getActivity(), "Cam was switched", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.d(TAG, "Camera was switched!");
                }
            }
        });


        cameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                if (cameraState != CameraState.DISABLED_FROM_USER) {
                    toggleCamera(false);
                    cameraState = CameraState.DISABLED_FROM_USER;
                } else {
                    toggleCamera(true);
                    cameraState = CameraState.ENABLED_FROM_USER;
                }
//                }

            }
        });

        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    Log.d(TAG, "Dynamic switched!");
                    ((CallActivity) getActivity()).getCurrentSession().switchAudioOutput();
                }
            }
        });

        micToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    if (isAudioEnabled) {
                        Log.d(TAG, "Mic is off!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        Log.d(TAG, "Mic is on!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

//                ((CallActivity) getActivity()).getCurrentSession().hangUp(userInfo);
                ((CallActivity) getActivity()).hangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        // TODO temporary insertion will be removed when GLVideoView will be fixed
        DisplayMetrics displaymetrics = new DisplayMetrics();
        displaymetrics.setToDefaults();

        ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

        layoutParams.height = localVideoView.getHeight();
        layoutParams.width = localVideoView.getWidth();

        imgMyCameraOff.setLayoutParams(layoutParams);

        Log.d(TAG, "Width is: " + imgMyCameraOff.getLayoutParams().width + " height is:" + imgMyCameraOff.getLayoutParams().height);
        // TODO end

        if (((CallActivity) getActivity()).getCurrentSession() != null) {
            ((CallActivity) getActivity()).getCurrentSession().setVideoEnabled(isNeedEnableCam);
            cameraToggle.setChecked(isNeedEnableCam);

            if (isNeedEnableCam) {
                Log.d(TAG, "Camera is on!");
                switchCameraToggle.setVisibility(View.VISIBLE);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
            } else {
                Log.d(TAG, "Camera is off!");
                switchCameraToggle.setVisibility(View.INVISIBLE);
                imgMyCameraOff.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        if (isPeerToPeerCall) {
            OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(opponents.get(0).getId());
            if (itemHolder == null) {
                return;
            }
            RTCGlVIew remoteVideoView = itemHolder.getOpponentView();
            if (remoteVideoView != null) {
                fillVideoView(remoteVideoView, videoTrack, false);
            }
            return;
        }
        if (localVideoView != null) {
            fillVideoView(localVideoView, videoTrack, true);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
        if (itemHolder == null) {
            return;
        }
        RTCGlVIew remoteVideoView = itemHolder.getOpponentView();
        if (remoteVideoView != null) {
            fillVideoView(remoteVideoView, videoTrack);
        }
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.append(userID, holder);
            }
        }
        return holder;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID){
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            Log.d(TAG, "getViewForOpponent holder user id is : " + childViewHolder.getUserId());
            if (userID.equals(childViewHolder.getUserId())) {
               return childViewHolder;
            }
        }
        return null;
    }

    private void fillVideoView(RTCGlVIew videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        //VideoRenderer remouteRenderer = new VideoRenderer(new VideoCallBacks(videoView, QBGLVideoView.Endpoint.REMOTE));
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ? videoView.obtainMainVideoRenderer() :
                videoView.obtainSecondVideoRenderer()));
        //videoView.setVideoTrack(videoTrack, QBGLVideoView.Endpoint.REMOTE);
        Log.d(TAG, (remoteRenderer ? "remote" : "local")+" Track is rendering");
    }

    private void fillVideoView(RTCGlVIew videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null){
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.setStatus(status);
            }
        });
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionButtonsEnabled(true);
            }
        });
        setStatusForOpponent(integer, getString(R.string.connected));
    }


    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.closed));
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.noAnswer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.hungUp));
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/) {
                dynamicToggleVideoCall.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                dynamicToggleVideoCall.setChecked(true);
            } else {
//                Toast.makeText(context, "Output audio stream is incorrect", Toast.LENGTH_LONG).show();
            }
            dynamicToggleVideoCall.invalidate();
        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration{

        private int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider){
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }

    }
}


