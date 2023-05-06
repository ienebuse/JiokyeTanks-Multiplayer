package com.jiokye.tankbattle_multiplayer.model;

import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.tank.Bullet;
import com.jiokye.tankbattle_multiplayer.tank.Enemy;
import com.jiokye.tankbattle_multiplayer.tank.HVE;
import com.jiokye.tankbattle_multiplayer.tank.Player;
import com.jiokye.tankbattle_multiplayer.tank.TankView;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.io.Serializable;
import java.util.ArrayList;

public class TankGameModel extends Game implements Serializable {
    public boolean server = WifiDirectManager.getInstance().isServer();
    public long time;
    public boolean playerInfo = false;
    public boolean playerReady = false;
    public String ip;
    public MTank[] mEnemies = new MTank[CONST.Tank.NUM_ENEMIES];
    public ArrayList<int[]>[] mEbullets;

    {
        mEbullets = new ArrayList[CONST.Tank.NUM_ENEMIES];
        for(int i = 0; i < CONST.Tank.NUM_ENEMIES; i++) {
            mEbullets[i] = new ArrayList<>();
        }
    }

    public int eCount = 0;

    //    public ArrayList<int[]> mEbullets = new ArrayList<>();
    public ArrayList<int[]> lObjects = new ArrayList<>();
    public ArrayList<Integer> lBushes = new ArrayList<>();
    public char[][] constLevel = null;
    public MTank mPlayer;
    public int height = TankView.HEIGHT;
    public boolean constructionLevelReq = false;
    public boolean gameOver = false;
    public boolean stageComplete = false;
    public boolean pause = false;
    public boolean resume = false;
    public boolean restart = false;
    public boolean end_game = false;
    public boolean gift_life = false;
    public boolean gift_game = false;
    public boolean gift_gold = false;
    public boolean eagleDestroyed;
    public boolean mlevelInfo = false;
    public int mlevel = 0;
    public int sceneSound = Sounds.TANK.FIGHT_SCENE1;
    public int eagleProtection = 0;
    public int[] bonus;
    public boolean bnsAv;
    public boolean bnsClr;
    public boolean goldAvailable = false;
    public boolean goldTaken = false;

    public int[] kills;
    public int totalKills;
    public int totalScore;
    public int stageScore;
    public boolean hveViewing = false;

    public void loadEnemies(Enemy[] enemies) {
        for(Enemy enemy:enemies) {
            if(enemy == null) {
                continue;
            }
//            mEnemies.add(new MTank(enemy.type,enemy.x,enemy.y,enemy.getDirection()));

            if(enemy.isDestroyed() && !enemy.svrKill && WifiDirectManager.getInstance().isServer()) {
                continue;
            }

            if(enemy.isDestroyed() && enemy.svrKill && !WifiDirectManager.getInstance().isServer()) {
                continue;
            }
            if(enemy instanceof HVE) {
                mEnemies[enemy.id] = new MTank((HVE)enemy);
            }
            else {
                mEnemies[enemy.id] = new MTank(enemy);
            }
        }
    }

    public void loadEnemyActiveBullets(ArrayList<Bullet>[] ebulltes) {
        for(int i = 0; i < ebulltes.length; i++) {
            if(ebulltes[i] == null) {
                continue;
            }
            for (Bullet b : ebulltes[i]) {
                if (b == null) {
                    continue;
                }
                //            if(b.isDestroyed() && !b.svrKill && WifiDirectManager.getInstance().isServer()) {
                //                continue;
                //            }
                //
                //            if(b.isDestroyed() && b.svrKill && !WifiDirectManager.getInstance().isServer()) {
                //                continue;
                //            }

                int[] bt = {
                        b.x,
                        b.y,
                        b.getDirection(),
                        b.isDestroyed() ? 1 : 0,
                        b.id,
                        b.svrKill ? 1 : 0,
                        b.explode ? 1 : 0,
                        b.launched ? 1 : 0};
                mEbullets[i].add(bt);
            }
        }
    }

    public void loadPlayer(Player p) {
//        if(p.isDestroyed() && !p.svrKill && WifiDirectManager.getInstance().isServer()) {
//            return;
//        }
//        if(p.isDestroyed() && p.svrKill && !WifiDirectManager.getInstance().isServer()) {
//            return;
//        }
        mPlayer = new MTank(p);
    }

    public void loadLevelObjects(ArrayList<int[]> lo) {
        for(int[] l: lo) {
            lObjects.add(l);
        }
        lObjects.trimToSize();
    }

    public void loadLevelBushes(ArrayList<Integer> lb) {
        for(int l: lb) {
            lBushes.add(l);
        }
        lBushes.trimToSize();
    }

    public void loadBonus(int x, int y, int b, boolean av, boolean cl, int id) {
        bonus = new int[] {x, y, b, id};
        bnsAv = av;
        bnsClr = cl;
    }

    public void loadPlayerKills(int  kills[]) {
        this.kills = new int[kills.length];
        for (int i=0; i < kills.length; i++) {
            this.kills[i] = kills[i];
        }
    }

    public void loadConstructionLevel(char[][] level) {
        constLevel = new char[26][26];
        for(int i = 0; i < 26; i++) {
            for(int j = 0; j < 26; j++) {
                constLevel[i][j] = level[i][j];
            }
        }

    }
}
