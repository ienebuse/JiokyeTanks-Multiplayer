package com.jiokye.tankbattle_multiplayer.wifidirect;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.connection.TCPClientConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.TCPClientSenderThread;
import com.jiokye.tankbattle_multiplayer.connection.TCPServerConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.TCPServerSenderThread;
import com.jiokye.tankbattle_multiplayer.connection.UDPClientConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.UDPClientSenderThread;
import com.jiokye.tankbattle_multiplayer.connection.UDPServerConnectionThread;
import com.jiokye.tankbattle_multiplayer.connection.UDPServerSenderThread;
import com.jiokye.tankbattle_multiplayer.model.Game;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.ClientHandler;
import com.jiokye.tankbattle_multiplayer.utility.ServerHandler;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WifiDirectManager implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {

    private static final String TAG = "WifiDirectManager";
    private static final WifiDirectManager instance = new WifiDirectManager();
    private AsyncTask<Void, Void, Integer> task = null;
    private boolean isServer;
    private String hostAddress = null;

    AppCompatActivity activity;
    IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager wManager = null;
    private WifiP2pManager.Channel wChannel;
    WiFiDirectBroadcastReceiver bReceiver;

    private final List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ArrayAdapter adapter;
    private List<String> peersName;
    private WifiP2pDevice connectedDevice = null;
    private TextView connectedDeviceView;

    TextView conStateView = null;
    ImageView playerView = null;
    Button confirmBtn = null;

    OnDisconnectListener onDisconnectListener;

    public static WifiDirectManager getInstance() {
        return instance;
    }

    public static ServerHandler serverHandler;
    public static ClientHandler clientHandler;
    public static UDPServerConnectionThread udpsvConn;
    public static UDPClientConnectionThread udpclConn;
    public static TCPServerConnectionThread tcpsvConn;
    public static TCPClientConnectionThread tcpclConn;

    public static UDPServerSenderThread udpsvSender;
    public static UDPClientSenderThread udpclSender;
    public static TCPServerSenderThread tcpsvSender;
    public static TCPClientSenderThread tcpclSender;

    private String deviceIP;

    public void initialize(AppCompatActivity activity) {
        this.activity = activity;
        wManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        wChannel = wManager.initialize(activity, activity.getMainLooper(), null);
        bReceiver = new WiFiDirectBroadcastReceiver(wManager, wChannel, activity);

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void connect(TextView conState) {
        this.conStateView = conState;
        connect(connectedDevice);
    }

    public void connect(WifiP2pDevice peer) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("Connection", "Permission not granted");
            return;
        }

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = connectedDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 15;
//        connectedDevice = peer;

        wManager.connect(wChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                Log.d("Connection: ", "Connection successful");
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                Log.d("Connection: ", "Connection unsuccessful");
                if(conStateView != null) {
                    conStateView.setText("Connection Failed");
                }
            }
        });
    }

    public void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("Player Search", "Permission not granted");
            return;
        }
        wManager.discoverPeers(wChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("Player Search", "Got players");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("Player Search", "Did not get any players");
            }
        });
    }

    public void registerBReceiver(){
        activity.registerReceiver(bReceiver, mIntentFilter);
    }

    public void unregisterBReceiver() {
        activity.unregisterReceiver(bReceiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // String from WifiP2pInfo struct
//        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        // After the group negotiation, we can determine the group owner
        // (server).

        hostAddress = info.groupOwnerAddress.getHostAddress();
        deviceIP = getLocalIpAddress();
        Log.d("DEVICE ADDRESS", deviceIP);


        Log.d("Group owner: ",hostAddress);


        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.



            serverHandler = new ServerHandler();
            if(TCPServerConnectionThread.socketListener != null) {
                TCPServerConnectionThread.socketListener.disconnect();
            }
            tcpsvConn = new TCPServerConnectionThread();
            tcpsvConn.setConnectListener(new TCPServerConnectionThread.OnConnect() {
                @Override
                public void connectStatus(boolean connected) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(conStateView != null) {
                                SoundManager.playSound(Sounds.TANK.CONNECT);
                                conStateView.setText("Connected");
                                confirmBtn.setAlpha(1f);
                                confirmBtn.setEnabled(true);

                                String name = getDeviceName();
                                if(name != null) {
                                    connectedDeviceView.setText("Invited " + name);
                                }
                            }

                            playerView.setBackground(ResourcesCompat.getDrawable(playerView.getResources(), R.drawable.p1,null));

                        }
                    });
                }
            });
            tcpsvConn.start();

            setIsServer(true);
            Log.d("Group Info", "This is the group leader");

        } else if (info.groupFormed) {
//            String hostName = info.groupOwnerAddress.getHostName();
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            clientHandler = new ClientHandler();
            if(TCPClientConnectionThread.clientListener != null) {
                TCPClientConnectionThread.clientListener.disconnect();
            }

            this.connectedDeviceView.setText("Joined game");
            this.playerView.setBackground(ResourcesCompat.getDrawable(playerView.getResources(), R.drawable.p2,null));

//            clConn = new ClientConnectionThread(connectedDevice.deviceName,hostAddress);
//            clConn = new ClientConnectionThread(hostName,hostAddress);
            tcpclConn = new TCPClientConnectionThread(hostAddress);
            tcpclConn.setConnectListener(new TCPClientConnectionThread.OnConnect() {
                @Override
                public void connectStatus(boolean connected) {
                    if(connected) {
                        SoundManager.playSound(Sounds.TANK.CONNECT);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                conStateView.setText("Connected");
                                confirmBtn.setAlpha(1f);
                                confirmBtn.setEnabled(true);
                            }
                        });

                    }
                }
            });
            tcpclConn.start();

            setIsServer(false);
            Log.d("Group Info", "This is not the group leader ==>");
        }
    }

    public void startUDPService(String ip) {
        try{
            if (isServer && !UDPServerConnectionThread.serverStarted) {
                if (UDPServerConnectionThread.socketListener != null) {
                    UDPServerConnectionThread.socketListener.disconnect();
                }
//                udpsvConn = new UDPServerConnectionThread(ip);
                udpsvConn = new UDPServerConnectionThread(getBroadcastAddress());
                udpsvConn.setConnectListener(new UDPClientConnectionThread.OnConnect() {
                    @Override
                    public void connectStatus(boolean connected) {
                        Log.d("UDP CONNECTION", "UDP server Connection successful");
                    }
                });
                udpsvConn.start();
            } else if(!isServer && !UDPClientConnectionThread.serverStarted) {
                if (UDPClientConnectionThread.clientListener != null) {
                    UDPClientConnectionThread.clientListener.disconnect();
                }
//                udpclConn = new UDPClientConnectionThread(ip);
                udpclConn = new UDPClientConnectionThread(getBroadcastAddress());
                udpclConn.setConnectListener(new UDPClientConnectionThread.OnConnect() {
                    @Override
                    public void connectStatus(boolean connected) {
                        Log.d("UDP CONNECTION", "UDP client Connection successful");
                    }
                });
                udpclConn.start();
            }
        }
//        catch (UnknownHostException e) {
//            e.printStackTrace();
//            terminateTask();
//            Log.d("UDP CONNECTION", "UDP Connection failed");
//        }
        catch (Exception e) {
            e.printStackTrace();
            terminateTask();
            Log.d("UDP CONNECTION", "UDP Connection failed");
        }
    }

//    private String getLocalIpAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }

    public String getLocalIpAddress() {
        WifiManager wifiMgr = (WifiManager) ((Context)activity).getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        return ipAddress;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    InetAddress getBroadcastAddress() {
        try{
            WifiManager wifi = (WifiManager) ((Context) activity).getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();

            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            return InetAddress.getByAddress(quads);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d("BROADCAST ADDRESS", "Could not get broadcast address");
        }
        return null;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d("Device: ", "Devices available");
        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        peersName.clear();
        Log.d("Device: ", String.valueOf(wifiP2pDeviceList.getDeviceList().size()) + " devices available");
        for (WifiP2pDevice peer : peers) {
            Log.d("Device: ", peer.deviceName);
            peersName.add(peer.deviceName);
        }
        adapter.notifyDataSetChanged();
    }

    public void registerDeviceView(ArrayAdapter adapter, List<String>names, TextView connectedDeviceView) {
        this.adapter = adapter;
        this.peersName = names;
        this.connectedDeviceView = connectedDeviceView;
        String name = getDeviceName();
        if(name != null) {
            this.connectedDeviceView.setText(name);
        }
    }

    public void registerConStatusView(TextView conStatus) {
        this.conStateView = conStatus;
    }

    public void registerPlayerView(ImageView playerView) {
        this.playerView = playerView;
    }

    public void registerConfirmBtn(Button confirmBtn) {
        this.confirmBtn = confirmBtn;
    }

    public void getHostAddress() {

    }

//    public void showDeviceWindow(AppCompatActivity activity) {
//        WifiDialog wd = new WifiDialog(activity);
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(wd.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        wd.show();
//        wd.getWindow().setAttributes(lp);
//    }

    public WifiP2pDevice getDevice(String peerName) {
        if (peers.size() != 0) {
            for (WifiP2pDevice peer : peers) {
                if (peer.deviceName.equals(peerName)) {
                    Log.d("Device: ", "Selected " + peer.deviceName);
                    connectedDevice = peer;
                    return connectedDevice;
                }
            }
        }
        return null;
    }

    public String getDeviceName() {
        if(connectedDevice != null) {
            return connectedDevice.deviceName;
        }
        return null;
    }

    public boolean isConnected() {
        switch (connectedDevice.status){
            case WifiP2pDevice.CONNECTED:
                Log.d("WIFI STATE: ","CONNECTED");
                break;
            case WifiP2pDevice.AVAILABLE:
                Log.d("WIFI STATE: ","AVAILABLE");
                break;
            case WifiP2pDevice.INVITED:
                Log.d("WIFI STATE: ","INVITED");
                break;
            case WifiP2pDevice.UNAVAILABLE:
                Log.d("WIFI STATE: ","UNAVAILABLE");
                break;
            case WifiP2pDevice.FAILED:
                Log.d("WIFI STATE: ","FAILED");
                break;
        }
        return connectedDevice != null && connectedDevice.status == WifiP2pDevice.CONNECTED;
    }

    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (wManager != null) {
            if (connectedDevice == null
                    || connectedDevice.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (connectedDevice.status == WifiP2pDevice.AVAILABLE
                    || connectedDevice.status == WifiP2pDevice.INVITED) {
                wManager.cancelConnect(wChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                    }
                });
            }
        }
    }

    public void disconnect() {
        disconnectOnly();
//        this.onChannelDisconnected();
    }

    private void disconnectOnly() {
        wManager.removeGroup(wChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
            }

        });
        terminateTask();
        this.connectedDevice = null;
    }

    private void terminateTask() {
        if(udpsvSender != null) {
            udpsvSender.disconnect();
        }
        if(udpclSender != null) {
            udpclSender.disconnect();
        }

        if(tcpsvSender != null) {
            tcpsvSender.disconnect();
        }
        if(tcpclSender != null) {
            tcpclSender.disconnect();
        }
    }

    private void setIsServer(boolean isServer) {
        this.isServer = isServer;
    }

    public boolean isServer() {
        return isServer;
    }

    public void sendTCPMessage(Game gameObject) {
        if(isServer()) {
            if(TCPServerConnectionThread.serverStarted) {
                ServerHandler.sendTcpToClient(gameObject);
            }
        }
        else {
//            ConnectionManager.getInstance().pushOutData(message);
            if(TCPClientConnectionThread.serverStarted) {
                ClientHandler.sendTcpToServer(gameObject);
            }
        }

    }

    public void sendUDPMessage(Game gameObject) {
        if(isServer()) {
            if(UDPServerConnectionThread.serverStarted) {
                ServerHandler.sendUdpToClient(gameObject);
            }
        }
        else {
//            ConnectionManager.getInstance().pushOutData(message);
            if(TCPClientConnectionThread.serverStarted) {
                ClientHandler.sendUdpToServer(gameObject);
            }
        }

    }

    public void sendTCPMessage(String message) {
        if(isServer()) {
            if(TCPServerConnectionThread.serverStarted) {
                ServerHandler.sendTcpToClient(message);
            }
        }
        else {
//            ConnectionManager.getInstance().pushOutData(message);
            if(TCPClientConnectionThread.serverStarted) {
                ClientHandler.sendTcpToServer(message);
            }
        }

    }

    public interface OnDisconnectListener {
        void handleDisconnection();
    }

    public void notifyDisconnect() {

        if(onDisconnectListener != null) {
            onDisconnectListener.handleDisconnection();
        }
    }

    public void setDisconnectListener(OnDisconnectListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }


}
