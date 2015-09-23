package com.quickblox.sample.videochatwebrtcnew.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;

import java.util.List;

/**
 * Created by tereha on 24.02.15.
 */
public class OpponentsFromCallAdapter extends RecyclerView.Adapter<OpponentsFromCallAdapter.ViewHolder> {

    public static final int OPPONENT = 1;
    public static final int HOLDER = 2;

    private static final int NUM_IN_ROW = 3;
    private static final String TAG = OpponentsFromCallAdapter.class.getSimpleName();
    private final int itemHeight;
    private final int itemWidth;

    private Context context;
    private List<QBUser> opponents;
    private boolean showVideoView;
    private LayoutInflater inflater;


    public OpponentsFromCallAdapter(Context context, List<QBUser> users, int width, int height, boolean showVideoView) {
        this.context = context;
        this.opponents = users;
        this.showVideoView = showVideoView;
        this.inflater = LayoutInflater.from(context);
        itemWidth = width;
        itemHeight = height;
        Log.d(TAG, "item width=" + itemWidth + ", item height=" + itemHeight);
    }

    @Override
    public int getItemCount() {
        return opponents.size();
    }

    public Integer getItem(int position) {
        return opponents.get(position).getId();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_opponent_from_call, null);
        v.setLayoutParams(new RecyclerView.LayoutParams(itemWidth, itemHeight));
        ViewHolder vh = new ViewHolder(v);
        vh.showOpponentView(showVideoView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final QBUser user = opponents.get(position);

        holder.opponentsName.setText(user.getFullName());
        holder.setUserId(user.getId());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView opponentsName;
    TextView connectionStatus;
    QBGLVideoView opponentView;
    private int userId;

    public ViewHolder(View itemView) {
        super(itemView);
        opponentsName = (TextView) itemView.findViewById(R.id.opponentName);
        connectionStatus = (TextView) itemView.findViewById(R.id.connectionStatus);
        opponentView = (QBGLVideoView) itemView.findViewById(R.id.opponentView);
    }

    public void setStatus(String status){
        connectionStatus.setText(status);
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public QBGLVideoView getOpponentView() {
        return opponentView;
    }

    public void showOpponentView(boolean show){
        opponentView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

}
