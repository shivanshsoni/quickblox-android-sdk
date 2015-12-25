package com.quickblox.sample.groupchatwebrtc.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.R;

import org.webrtc.SurfaceViewRenderer;

public class OneToOneConversationFragment extends ConversationFragment {

    SurfaceViewRenderer opponentViewRenderer;
    private TextView connectionStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateView(inflater, container, savedInstanceState, R.layout.one_to_one_conversation_fragment);
    }

    @Override
    protected void initCustomView(View view) {
        opponentViewRenderer = (SurfaceViewRenderer) view.findViewById(R.id.opponentView);
        connectionStatus = (TextView) view.findViewById(R.id.connectionStatus);
    }

    @Override
    protected TextView getStatusViewForOpponent(int userId) {
        return connectionStatus;
    }

    @Override
    protected SurfaceViewRenderer getVideoViewForOpponent(Integer userID) {
        return opponentViewRenderer;
    }
}
