package com.jiokye.tankbattle_multiplayer.wifidirect;

import android.app.Dialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

import java.util.ArrayList;
import java.util.List;

public class WifiDialog extends Dialog implements View.OnClickListener {
//    public AppCompatActivity activity;
    FragmentActivity activity;
    public Dialog d;
    public Button searchBtn, cancelBtn, confirmBtn;
    ListView playerListView;
    ArrayAdapter adapter;
    TextView connectedDeviceView;
    TextView conState;
    ImageView playerView;

    boolean p2Selected = false;
    OnWifiDialogResult mWifiDialogResult;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private List<String> peersName = new ArrayList<String>();


    public WifiDialog(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_wifi);
        setCancelable(false);
//
        playerListView = findViewById(R.id.search_list);
        connectedDeviceView = findViewById(R.id.connectedPlayer);
        conState = findViewById(R.id.connStatus);
        playerView = findViewById(R.id.playerView);

        connectedDeviceView.setText("");

        adapter = new ArrayAdapter<String>(playerListView.getContext(),
                R.layout.peers_list_view, peersName);
        playerListView.setAdapter(adapter);
        playerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String peerName = (String) ((TextView) view).getText();
                WifiP2pDevice device = WifiDirectManager.getInstance().getDevice(peerName);
                if (device != null) {
                    SoundManager.playSound(Sounds.TANK.CLICK);
                    conState.setText("Connecting");
                    WifiDirectManager.getInstance().connect(conState);
                }
            }
        });



        searchBtn = findViewById(R.id.searchBtn);
        cancelBtn = findViewById(R.id.wifiClose);
        confirmBtn = findViewById(R.id.wifiConfirm);
        searchBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha(0.5f);

        WifiDirectManager.getInstance().registerDeviceView(adapter, peersName, connectedDeviceView);
        WifiDirectManager.getInstance().registerConStatusView(conState);
        WifiDirectManager.getInstance().registerPlayerView(playerView);
        WifiDirectManager.getInstance().registerConfirmBtn(confirmBtn);

        findDevice();
    }

    private void findDevice() {
        SoundManager.playSound(Sounds.TANK.CLICK);
        WifiDirectManager.getInstance().cancelDisconnect();
        Log.d("Player Search", "Searching for players");
        TextView conState = findViewById(R.id.connStatus);
        conState.setText("Searching");
        WifiDirectManager.getInstance().discoverPeers();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Utils.Effects.blink(v,2).setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

                if (id == R.id.searchBtn) {
                    findDevice();
                }
                else if(id == R.id.wifiClose) {
                    SoundManager.playSound(Sounds.TANK.CLICK);
//            MessageRegister.getInstance().registerWifiDialog();
                    p2Selected = false;
                    mWifiDialogResult.finish(p2Selected);
                    dismiss();
                }
                else if(id == R.id.wifiConfirm) {
                    p2Selected = true;
                    SoundManager.playSound(Sounds.TANK.CLICK);
                    mWifiDialogResult.finish(p2Selected);
                    dismiss();
                }

            }
        });
    }

    public void setWifiDialogResult(OnWifiDialogResult dialogResult) {
        mWifiDialogResult = dialogResult;
    }

    public interface OnWifiDialogResult {
        void finish(boolean p2Selected);
    }

    public void onWindowFocusChanged (boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
