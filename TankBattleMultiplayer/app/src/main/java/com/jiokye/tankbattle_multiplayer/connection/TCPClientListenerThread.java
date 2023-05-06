package com.jiokye.tankbattle_multiplayer.connection;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class TCPClientListenerThread extends Thread{

    Socket socket;
    private boolean RUN = true;
    ObjectInputStream ois;
    InputStream is = null;
    BufferedInputStream bis;

    TCPClientListenerThread(Socket soc) {
        socket = soc;
    }

    @Override
    public void run() {
        try {
            is = socket.getInputStream();
//            bis = new BufferedInputStream(is);
            ois = new ObjectInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (RUN) {
            try {

                Bundle data = new Bundle();
                Object serverObject = ois.readObject();

                if (serverObject != null) {
                    if (serverObject instanceof String) {
//                        data.putSerializable(CONST.STRING_INFO, (String) serverObject);
                        //                        Log.d("CLIENT LISTENER", "GOT STRING");
                    } else if (serverObject instanceof Game) {
                        data.putSerializable(CONST.GAME_DATA_KEY, (Game) serverObject);
//                        MessageRegister.getInstance().registerNewMessage((Game)serverObject);
//                        Log.d("CLIENT LISTENER", "GOT GAME");
                    }
                    Message msg = new Message();
                    msg.setData(data);
                    WifiDirectManager.clientHandler.sendMessage(msg);
                }
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
                disconnect();
            } catch (IOException e) {
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
        if(socket != null) {
            try {
                socket.close();
                socket = null;
                TCPClientConnectionThread.serverStarted = false;
            } catch (IOException e) {
                e.printStackTrace();
                socket = null;
                TCPClientConnectionThread.serverStarted = false;
            }
            finally {
                Log.d("CLIENT CONNECTION: ", "Client disconnected");
            }
        }
    }
}
