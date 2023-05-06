package com.jiokye.tankbattle_multiplayer.connection;

import android.util.Log;

import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPServerConnectionThread extends Thread{

    //todo socket
//    static final int SocketServerPORT = 8888;
//    public static Socket socket = null;
//    public static ServerSocket serverSocket;
    static final int SocketServerPORT = 4445;
    public static DatagramSocket socket = null;
    public static boolean serverStarted = false;
    public static boolean allPlayersJoined = false;
    public static UDPServerListenerThread socketListener;
    private UDPClientConnectionThread.OnConnect connectListener;
    private InetAddress inetAddress;

    public UDPServerConnectionThread(String ipaddress) throws UnknownHostException {


        inetAddress = InetAddress.getByName(ipaddress);

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

    public UDPServerConnectionThread(InetAddress ipaddress) {


        this.inetAddress = ipaddress;

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
                socket = new DatagramSocket(SocketServerPORT);
                Log.d("SERVER CONNECTION", "CONNECTED");
                connectListener.connectStatus(true);
                socketListener = new UDPServerListenerThread(socket);
                socketListener.start();
                WifiDirectManager.udpsvSender = new UDPServerSenderThread(socket, inetAddress);
                WifiDirectManager.udpsvSender.start();
                serverStarted = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void setConnectListener(UDPClientConnectionThread.OnConnect listener) {
        connectListener = listener;
    }

    public interface OnConnect {
        void connectStatus(boolean connected);
    }
}
