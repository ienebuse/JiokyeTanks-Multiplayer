package com.jiokye.tankbattle_multiplayer.utility;

public interface ServiceListener {
    void onServiceMessageReceived(int games, long time_left, boolean h6);
}
