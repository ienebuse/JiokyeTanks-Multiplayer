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
import java.net.Socket;

public class TCPServerListenerThread extends Thread{

    private Socket hostThreadSocket;
    private boolean RUN = true;
    ObjectInputStream ois;
    InputStream is = null;
    BufferedInputStream bis;

    TCPServerListenerThread(Socket soc) {
        hostThreadSocket = soc;
    }

    @Override
    public void run() {
        try{
            is = hostThreadSocket.getInputStream();
//            bis = new BufferedInputStream(is);
            ois = new ObjectInputStream(is);
        }catch (IOException e) {
            e.printStackTrace();
        }
        while (RUN) {

            try {
                Object gameObject;
                Bundle data = new Bundle();
                gameObject = ois.readObject();
                if (gameObject != null) {
                    if (gameObject instanceof PlayerInfo) {
//                        data.putSerializable(CONST.PLAYER_INFO, (PlayerInfo) gameObject);
//                        Log.d("SERVER LISTENER", "GOT PLAYER");
//                        data.putInt(Constants.ACTION_KEY, CONST.PLAYER_INFO.PLAYER_LIST_UPDATE);
//                        ServerConnectionThread.socketUserMap.put(hostThreadSocket, ((PlayerInfo) gameObject).username);
                    } else {
                        data.putSerializable(CONST.GAME_DATA_KEY, (Game) gameObject);
                        MessageRegister.getInstance().registerNewMessage((Game)gameObject);
//                        Log.d("SERVER LISTENER", "GOT GAME");
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
                TCPServerConnectionThread.serverStarted = false;
            } catch (IOException e) {
                e.printStackTrace();
                hostThreadSocket = null;
                TCPServerConnectionThread.serverStarted = false;
            }
            finally {
                Log.d("SERVER CONNECTION: ", "Server disconnected");
            }
        }
    }
}
