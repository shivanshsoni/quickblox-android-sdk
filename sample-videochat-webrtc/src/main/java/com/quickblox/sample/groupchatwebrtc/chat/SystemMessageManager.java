package com.quickblox.sample.groupchatwebrtc.chat;


import android.content.Context;
import android.text.TextUtils;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.utils.DeviceInfoUtils;
import com.quickblox.videochat.webrtc.QBSignalingSpec;

import org.jivesoftware.smack.SmackException;

public class SystemMessageManager implements QBSystemMessageListener {

    private static final String DEVICE_ID = "device_id";

    private final QBSystemMessagesManager systemMessagesManager;
    private final String deviceId;

    private MultiDeviceStatusListener multiDeviceStatusListener;

    public SystemMessageManager(QBChatService chatService, Context context) {

        systemMessagesManager = chatService.getSystemMessagesManager();

        deviceId = DeviceInfoUtils.getDeviceId(context);
        systemMessagesManager.addSystemMessageListener(this);
    }

    public void setMultiDeviceStatusListener(MultiDeviceStatusListener multiDeviceStatusListener) {
        this.multiDeviceStatusListener = multiDeviceStatusListener;
    }

    public void sendAcceptMessage(int userId) throws SmackException.NotConnectedException {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setRecipientId(userId);

        chatMessage.setProperty(QBSignalingSpec.QBSignalField.SIGNALING_TYPE.getValue(),
                QBSignalingSpec.QBSignalCMD.ACCEPT_CALL.getValue());
        chatMessage.setProperty(DEVICE_ID, deviceId);
        systemMessagesManager.sendSystemMessage(chatMessage);
    }

    public void sendRejecttMessage(int userId) throws SmackException.NotConnectedException {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setRecipientId(userId);

        chatMessage.setProperty(QBSignalingSpec.QBSignalField.SIGNALING_TYPE.getValue(),
                QBSignalingSpec.QBSignalCMD.REJECT_CALL.getValue());
        chatMessage.setProperty(DEVICE_ID, deviceId);
        systemMessagesManager.sendSystemMessage(chatMessage);
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        String signalingRawType = (String) qbChatMessage.getProperty(QBSignalingSpec.QBSignalField.SIGNALING_TYPE.getValue());

        QBSignalingSpec.QBSignalCMD signalingType;

        if (TextUtils.isEmpty(signalingRawType)) {
            return;
        }

        String msgDeviceId = (String) qbChatMessage.getProperty(DEVICE_ID);
        if (deviceId.equals(msgDeviceId)) {
            return;
        }

        signalingType = QBSignalingSpec.QBSignalCMD.getTypeByRawValue(signalingRawType);

        switch (signalingType) {
            case ACCEPT_CALL:
                if (multiDeviceStatusListener != null) {
                    multiDeviceStatusListener.onAcceptedCall((String) qbChatMessage.getProperty(DEVICE_ID),
                            qbChatMessage);
                }
                break;
            case REJECT_CALL:
                if (multiDeviceStatusListener != null) {
                    multiDeviceStatusListener.onRejectedCall((String) qbChatMessage.getProperty(DEVICE_ID),
                            qbChatMessage);
                }
                break;

        }
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {

    }

    public interface MultiDeviceStatusListener {

        public void onAcceptedCall(String deviceId, QBChatMessage chatMessage);

        public void onRejectedCall(String deviceId, QBChatMessage chatMessage);
    }
}
