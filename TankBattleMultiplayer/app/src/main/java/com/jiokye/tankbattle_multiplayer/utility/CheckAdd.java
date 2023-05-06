package com.jiokye.tankbattle_multiplayer.utility;

public class CheckAdd {

    private static CheckAdd instance;
    private int click_count = 0;
    private int transition_count = 0;
    private int clickThreshold = 2;
    private int transitionThreshold = 2;

    public static synchronized CheckAdd getInstance() {
        if(instance == null) {
            instance = new CheckAdd();
        }
        return instance;
    }

    public boolean click() {
        ++click_count;
//        if(click_count > clickThreshold && Math.random() < 0.8) {
        if(Math.random() < 0.8) {
            click_count = 0;
            return true;
        }
        return  false;
    }

    public boolean transition() {
        return  transition(0.8f);
    }

    public boolean transition(float prob) {
        ++click_count;
//        if(transition_count > transitionThreshold && Math.random() < 0.8) {
        if(Math.random() < prob) {
            transition_count = 0;
            return true;
        }
        return  false;
    }
}
