package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.ViewStubCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.activities.ListUsersActivity;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * QuickBlox team
 */
public abstract class ConversationFragment extends Fragment implements Serializable,
        QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks,
        CallActivity.QBRTCSessionUserCallback {

    private static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";

    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;

    protected boolean isVideoEnabled = false;
    protected SurfaceViewRenderer localVideoView;
    protected EglBase rootEglBase;
    protected View view;

    protected ArrayList<QBUser> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;

    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private View myCameraOff;
    private TextView incUserName;

    private Map<String, String> userInfo;
    private boolean isAudioEnabled = true;
    private LinearLayout actionVideoButtonsLayout;
    private String callerName;
    private boolean isMessageProcessed;

    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private CameraState cameraState = CameraState.NONE;
    private boolean isPeerToPeerCall;
    private Handler mainHandler;

    public static ConversationFragment newInstance(List<QBUser> opponents, String callerName,
                                                   QBRTCTypes.QBConferenceType qbConferenceType,
                                                   Map<String, String> userInfo,
                                                   CallActivity.StartConversetionReason reason,
                                                   String sesionnId) {
        ConversationFragment fragment = (opponents.size() == 1) ? new OneToOneConversationFragment() :
                new GroupConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.CONFERENCE_TYPE, qbConferenceType.getValue());
        bundle.putString(CALLER_NAME, callerName);
        bundle.putSerializable(Consts.OPPONENTS, (Serializable) opponents);
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

    protected abstract TextView getStatusViewForOpponent(int userId);

    protected abstract void initCustomView(View view);

    protected abstract SurfaceViewRenderer getVideoViewForOpponent(Integer userID);

    protected View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState,
                             int contentId) {
        view = inflater.inflate(R.layout.conversation_fragment, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        ((CallActivity) getActivity()).initActionBarWithTimer();

        if (getArguments() != null) {
            opponents = (ArrayList<QBUser>) getArguments().getSerializable(Consts.OPPONENTS);
            qbConferenceType = getArguments().getInt(Consts.CONFERENCE_TYPE);
            startReason = getArguments().getInt(CallActivity.START_CONVERSATION_REASON);
            sessionID = getArguments().getString(CallActivity.SESSION_ID);
            callerName = getArguments().getString(CallActivity.CALLER_NAME);

            isPeerToPeerCall = opponents.size() == 1;
            isVideoEnabled = (qbConferenceType ==
                    QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue());
            Log.d(TAG, "CALLER_NAME: " + callerName);
            Log.d(TAG, "opponents: " + opponents.toString());
        }


        rootEglBase = EglBase.create();
        initContentView(view, contentId);
        initViews(view);
        initButtonsListener();
        initSessionListener();
        setUpUiByCallType(qbConferenceType);

        mainHandler = new FragmentLifeCycleHandler();
        return view;
    }

    private void initSessionListener() {
        ((CallActivity) getActivity()).addVideoTrackCallbacksListener(this);
    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (!isVideoEnabled) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {

        cameraToggle.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);

        switchCameraToggle.setEnabled(enability);
        switchCameraToggle.setActivated(enability);
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

    protected void initContentView(View view, int layoutID){
        ViewStub stubCompat = (ViewStub) view.findViewById(R.id.main_content);
        stubCompat.setLayoutResource(layoutID);
        stubCompat.inflate();
    }

    private void initViews(View view) {
        localVideoView = (SurfaceViewRenderer) view.findViewById(R.id.localSurfView);
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        localVideoView.setZOrderMediaOverlay(true);
        initLocalViewUI(view);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        incUserName = (TextView) view.findViewById(R.id.incUserName);
        incUserName.setText(callerName);
        incUserName.setBackgroundResource(ListUsersActivity.selectBackgrounForOpponent((
                DataHolder.getUserIndexByFullName(callerName)) + 1));
        actionButtonsEnabled(false);
        initCustomView(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && isVideoEnabled) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && isVideoEnabled) {
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

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
        SurfaceViewRenderer remoteVideoView = getVideoViewForOpponent(userID);
        if (remoteVideoView != null) {
            remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
            fillVideoView(remoteVideoView, videoTrack);
        }
    }

    private void initSwitchCameraButton(View view) {
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        switchCameraToggle.setVisibility(isVideoEnabled ?
                View.VISIBLE : View.INVISIBLE);
    }

    private void initButtonsListener() {
        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                toggleCamera(isChecked);
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
                QBRTCSession currentSession = ((CallActivity) getActivity()).getCurrentSession();
                if (currentSession != null) {
                    currentSession.getMediaStreamManager().setAudioEnabled(!isAudioEnabled);
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                Log.d(TAG, "Call is stopped");

                ((CallActivity) getActivity()).hangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });

        switchCameraToggle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QBRTCSession currentSession = ((CallActivity) getActivity()).getCurrentSession();
                if (currentSession == null) {
                    return;
                }
                final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                if (mediaStreamManager == null) {
                    return;
                }
                mediaStreamManager.switchCameraInput(
                        new VideoCapturerAndroid.CameraSwitchHandler() {
                            @Override
                            public void onCameraSwitchDone(boolean b) {
                               runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toggleCamerainternal(mediaStreamManager);
                                    }
                                });
                            }

                            @Override
                            public void onCameraSwitchError(String s) {

                            }

                        });
            }

        });
    }

    private void toggleCamerainternal(QBMediaStreamManager mediaStreamManager){
        toggleCameraOnUiThread(false);
        Log.d(TAG, "Camera was switched!");
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleCameraOnUiThread(true);
            }
        }, TOGGLE_CAMERA_DELAY);
    }

    private void toggleCameraOnUiThread(final boolean toggle){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleCamera(toggle);
            }
        });
    }

    protected void runOnUiThread(Runnable runnable){
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = ((CallActivity) getActivity()).getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null){
            currentSession.getMediaStreamManager().setVideoEnabled(isNeedEnableCam);
                myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
                switchCameraToggle.setVisibility(isNeedEnableCam ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");

        if (localVideoView != null) {
            fillVideoView(localVideoView, videoTrack, !isPeerToPeerCall);
        }
    }

    private void initLocalViewUI(View localView) {
        initSwitchCameraButton(localView);
        myCameraOff = localView.findViewById(R.id.cameraOff);
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(videoView));
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(SurfaceViewRenderer videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        final TextView opponentView = getStatusViewForOpponent(userId);
        if (opponentView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                opponentView.setText(status);
            }
        });
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession,final Integer userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionButtonsEnabled(true);
            }
        });
        setStatusForOpponent(userId, getString(R.string.connected));
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

            dynamicToggleVideoCall.setChecked(intent.getIntExtra("state", -1) == 1);

        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class FragmentLifeCycleHandler extends Handler{

        @Override
        public void dispatchMessage(Message msg) {
            if (isAdded() && getActivity() != null) {
                super.dispatchMessage(msg);
            } else {
                Log.d(TAG, "Fragment under destroying");
            }
        }
    }

}


