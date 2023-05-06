package com.jiokye.tankbattle_multiplayer.connection;

import android.util.Log;

import com.jiokye.tankbattle_multiplayer.utility.PlayerInfo;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClientConnectionThread extends Thread{

    public static Socket socket;
    String dstAddress;
    int dstPort = 8080;
    public static boolean serverStarted = false;
    String userName;
    public static TCPClientListenerThread clientListener;
    private OnConnect connectListener;

    public TCPClientConnectionThread(String userName, String dstAddress) {
        this.userName = userName;
        this.dstAddress = dstAddress;
    }

    public TCPClientConnectionThread(String dstAddress) {
        this.userName = null;
        this.dstAddress = dstAddress;
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                socket = null;
            }
        }
    }

    @Override
    public void run() {
        if (socket == null) {
            try {
                if (dstAddress != null) {
                    socket = new Socket(dstAddress, dstPort);
                    if (socket.isConnected()) {
                        Log.d("CLIENT CONNECTION", "CONNECTED");
                        connectListener.connectStatus(true);
                        clientListener = new TCPClientListenerThread(socket);
                        clientListener.start();
                        PlayerInfo playerInfo = new PlayerInfo(userName);
                        WifiDirectManager.tcpclSender = new TCPClientSenderThread(socket, playerInfo);
                        WifiDirectManager.tcpclSender.start();
                        serverStarted = true;
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConnectListener(OnConnect listener) {
        connectListener = listener;
    }

    public interface OnConnect {
        void connectStatus(boolean connected);
    }
}
