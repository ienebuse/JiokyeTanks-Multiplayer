package com.jiokye.tankbattle_multiplayer.utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

public class ServerHandler extends Handler {

    Bundle messageData;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        messageData = msg.getData();
        Object serverObject = messageData.getSerializable(CONST.GAME_DATA_KEY);
        if (serverObject instanceof Game) {
            // TODO
            MessageRegister.getInstance().registerNewMessage((Game)serverObject);
        }
    }

    public static void sendTcpToClient(Object gameObject) {
        WifiDirectManager.tcpsvSender.sendMessage(gameObject);
    }

    public static void sendUdpToClient(Object gameObject) {
        WifiDirectManager.udpsvSender.sendMessage(gameObject);
    }
}
