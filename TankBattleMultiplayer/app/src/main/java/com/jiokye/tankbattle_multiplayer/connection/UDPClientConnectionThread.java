package com.jiokye.tankbattle_multiplayer.connection;

import android.util.Log;

import com.jiokye.tankbattle_multiplayer.utility.PlayerInfo;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPClientConnectionThread extends Thread{

    //todo socket
//    public static Socket socket;
    public static DatagramSocket socket;
    InetAddress dstAddress;
    //todo socket
//    int dstPort = 8888;
    int dstPort = 4445;
    public static boolean serverStarted = false;
    String userName;
    public static UDPClientListenerThread clientListener;
    private OnConnect connectListener;

//    public UDPClientConnectionThread(String userName, String dstAddress) {
//        this.userName = userName;
//        this.dstAddress = dstAddress;
//    }

    public UDPClientConnectionThread(String dstAddress)  throws UnknownHostException {
        this.userName = null;
//        this.dstAddress = dstAddress;
        this.dstAddress = InetAddress.getByName(dstAddress);
        if(socket != null) {
            try {
                socket.close();
            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                socket = null;
            }
        }
    }

    public UDPClientConnectionThread(InetAddress dstAddress) {
        this.userName = null;
//        this.dstAddress = dstAddress;
        this.dstAddress = dstAddress;
        if(socket != null) {
            try {
                socket.close();
            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
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
//                if (dstAddress != null) {
                    //todo socket
//                    socket = new Socket(dstAddress, dstPort);
                    socket = new DatagramSocket(dstPort);
                    socket.setBroadcast(true);
//                    if (socket.isConnected()) {
                        Log.d("CLIENT CONNECTION", "CONNECTED");
                        connectListener.connectStatus(true);
                        clientListener = new UDPClientListenerThread(socket);
                        clientListener.start();
                        PlayerInfo playerInfo = new PlayerInfo(userName);
                        WifiDirectManager.udpclSender = new UDPClientSenderThread(socket, dstAddress);
                        WifiDirectManager.udpclSender.start();
                        serverStarted = true;
//                    }
//                }
            }
//            catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
            catch (IOException e) {
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
