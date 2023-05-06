package com.jiokye.tankbattle_multiplayer.utility;

import com.jiokye.tankbattle_multiplayer.model.Game;

public interface RemoteMessageListener {
    void onMessageReceived(Game message);
}

