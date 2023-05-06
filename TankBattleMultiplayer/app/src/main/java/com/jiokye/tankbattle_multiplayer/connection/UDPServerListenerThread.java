package com.jiokye.tankbattle_multiplayer.connection;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.utility.MessageRegister;
import com.jiokye.tankbattle_multiplayer.utility.PlayerInfo;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServerListenerThread extends Thread{

    //todo socket
//    private Socket hostThreadSocket;
    private DatagramSocket hostThreadSocket;
    private boolean RUN = true;
    ObjectInputStream ois;
    InputStream is = null;
    BufferedInputStream bis;

    //todo socket
//    ServerListenerThread(Socket soc) {
    UDPServerListenerThread(DatagramSocket soc) {
        hostThreadSocket = soc;
    }

    @Override
    public void run() {
        byte[] receivedByte = new byte[65536];
        DatagramPacket dp;

        while (RUN) {

            try {

                Bundle data = new Bundle();
                //todo socket
                dp = new DatagramPacket(receivedByte, receivedByte.length);
                hostThreadSocket.receive(dp);
                Object gameObject = ConnUtil.deserialize(dp.getData());

                if (gameObject != null) {
                    if (gameObject instanceof PlayerInfo) {
//                        data.putSerializable(CONST.PLAYER_INFO, (PlayerInfo) gameObject);
//                        Log.d("SERVER LISTENER", "GOT PLAYER");
//                        data.putInt(Constants.ACTION_KEY, CONST.PLAYER_INFO.PLAYER_LIST_UPDATE);
//                        ServerConnectionThread.socketUserMap.put(hostThreadSocket, ((PlayerInfo) gameObject).username);
                    } else {
                        data.putSerializable(CONST.GAME_DATA_KEY, (Game) gameObject);
                        MessageRegister.getInstance().registerNewMessage((Game)gameObject);
//                        Log.d("UDP SERVER LISTENER", "GOT GAME");
                    }
                    Message msg = new Message();
                    msg.setData(data);
                    WifiDirectManager.serverHandler.sendMessage(msg);
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                disconnect();
            }
            catch (Exception e){
                e.printStackTrace();
                disconnect();
            }
        }
    }

    public void disconnect() {
        RUN = false;
        WifiDirectManager.getInstance().notifyDisconnect();
        if(hostThreadSocket != null) {
            try {
                hostThreadSocket.close();
                hostThreadSocket = null;
                UDPServerConnectionThread.serverStarted = false;
            }
//            catch (IOException e) {
//                e.printStackTrace();
//                hostThreadSocket = null;
//                ServerConnectionThread.serverStarted = false;
//            }
            finally {
                Log.d("SERVER CONNECTION: ", "Server disconnected");
            }
        }
    }
}
