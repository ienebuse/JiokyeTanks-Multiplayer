package com.jiokye.tankbattle_multiplayer.connection;

import android.util.Log;

import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerConnectionThread extends Thread{

    static final int SocketServerPORT = 8080;
    public static Socket socket = null;
    public static boolean serverStarted = false;
    public static ServerSocket serverSocket;
    public static boolean allPlayersJoined = false;
    public static TCPServerListenerThread socketListener;
    private TCPServerConnectionThread.OnConnect connectListener;

    public TCPServerConnectionThread() {
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                serverSocket = null;
            }
        }
    }

    @Override
    public void run() {
//        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                socket = serverSocket.accept();
                Log.d("SERVER CONNECTION", "CONNECTED");
                connectListener.connectStatus(true);
                socketListener = new TCPServerListenerThread(socket);
                socketListener.start();
                WifiDirectManager.tcpsvSender = new TCPServerSenderThread(socket, CONST.GAME_NAME);
                WifiDirectManager.tcpsvSender.start();
                serverStarted = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
//        }
    }

    public void setConnectListener(TCPServerConnectionThread.OnConnect listener) {
        connectListener = listener;
    }

    public interface OnConnect {
        void connectStatus(boolean connected);
    }
}
