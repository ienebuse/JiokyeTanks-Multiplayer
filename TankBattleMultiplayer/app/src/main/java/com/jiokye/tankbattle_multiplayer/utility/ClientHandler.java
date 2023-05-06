package com.jiokye.tankbattle_multiplayer.utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

public class ClientHandler extends Handler {

    Bundle messageData;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        messageData = msg.getData();
        Object clientObject = messageData.getSerializable(CONST.GAME_DATA_KEY);
        if (clientObject instanceof Game) {
            MessageRegister.getInstance().registerNewMessage((Game)clientObject);
        }
    }

    public static void sendTcpToServer(Object gameObject) {
        WifiDirectManager.tcpclSender.sendMessage(gameObject);
    }

    public static void sendUdpToServer(Object gameObject) {
        WifiDirectManager.udpclSender.sendMessage(gameObject);
    }
}
