package com.jiokye.tankbattle_multiplayer.connection;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPServerSenderThread extends Thread{

    //todo socket
//    private final Socket hostThreadSocket;
    private final DatagramSocket hostThreadSocket;
//    Object message;
    private boolean msgAvailable = true;
    private OutputStream os;
    private ObjectOutputStream oos;
    private BufferedOutputStream bos;
    private final ConcurrentLinkedQueue<Object> messages;
    private boolean RUN = true;
    private InetAddress inetAddress;
    static final int DPORT = 4445;
//    static final int DPORT = 8888;

    //todo socket
//    public ServerSenderThread(Socket socket, Object message) {
    public UDPServerSenderThread(DatagramSocket socket, InetAddress inetAddress) {
        messages = new ConcurrentLinkedQueue<>();
        hostThreadSocket = socket;
//        this.messages.add(message);
        this.inetAddress = inetAddress;
    }

    //todo socket
//    public ServerSenderThread(Socket socket) {
    public UDPServerSenderThread(DatagramSocket socket) {
        messages = new ConcurrentLinkedQueue<>();
        hostThreadSocket = socket;
    }

    @Override
    public void run() {
        DatagramPacket dp;
        byte[] sendByte;

        try {
            //todo socket
//            os = hostThreadSocket.getOutputStream();
//            bos = new BufferedOutputStream(os);
//            oos = new ObjectOutputStream(os);

            while(RUN) {
                while(!messages.isEmpty()) {
                    //todo socket
//                    oos.writeObject(messages.poll());
                    sendByte = ConnUtil.serialize(messages.poll());
                    dp = new DatagramPacket(sendByte,sendByte.length, inetAddress, DPORT);
                    hostThreadSocket.send(dp);
//                    Log.d("UDP SERVER SENDER", "SENT GAME");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Object message) {
        messages.add(message);
    }

    public void disconnect() {
        RUN = false;
        if(oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
