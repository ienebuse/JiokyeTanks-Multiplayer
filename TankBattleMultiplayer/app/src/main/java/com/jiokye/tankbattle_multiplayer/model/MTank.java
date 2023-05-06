package com.jiokye.tankbattle_multiplayer.model;

import com.jiokye.tankbattle_multiplayer.tank.Bullet;
import com.jiokye.tankbattle_multiplayer.tank.Enemy;
import com.jiokye.tankbattle_multiplayer.tank.HVE;
import com.jiokye.tankbattle_multiplayer.tank.ObjectType;
import com.jiokye.tankbattle_multiplayer.tank.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class MTank implements Serializable {
    public int x,y,sx,sy;
    public float v,bv;
    public ObjectType type;
    public int dirction;
    public int armour;
    public int lives;
    public boolean boat,shield;
    public boolean tDestroyed, respawn;
    public ArrayList<int[]>bullets;
    public int typeVal, group;
    public int id;
    public boolean hasBonus = false;
    public boolean svrKill = false;
    public int gotBonus = 0;
    public int player = 0;
    public boolean freeze = false;
    public boolean bBomb = false;
    public boolean hve = false;
    public boolean bombed = false;
    public int hits;
    public boolean mineDropped = false;
    public boolean mineMoving = false;
    public boolean mineExplode = false;
    public int mineID = -1;

    public  MTank(ObjectType type, int x, int y, int dirction) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.dirction = dirction;

    }

    public MTank(Player p) {
        x = p.x;
        y = p.y;
        type = p.type;
        dirction = p.getDirection();
        boat = p.hasBoat();
        shield = p.hasShield();
        armour = p.armour;
        lives = p.lives;
        tDestroyed = p.isDestroyed();
        respawn = p.respawn;
        svrKill = p.svrKill;
        gotBonus = p.gotBonus;
        player = p.player;
        freeze = p.isFrozen();
        bBomb = p.getBBomb();
//        if(!p.getMine().isMoving()) {
            mineDropped = p.getMine().isDropped();
            mineMoving = p.getMine().isMoving();
            mineID = p.getMine().id;
            mineExplode = p.getMine().isExploding();
//        }



        bullets = new ArrayList<>();
        ArrayList<Bullet> pBullets = p.getBullets();
        for(Bullet b:pBullets) {
            if(b == null || b.recycle) {
                continue;
            }
//            if(b.isDestroyed() && !b.svrKill && WifiDirectManager.getInstance().isServer()) {
//                continue;
//            }
//
//            if(b.isDestroyed() && b.svrKill && !WifiDirectManager.getInstance().isServer()) {
//                continue;
//            }

            if(b.isDestroyed()) {
                if(b.sent) {
                    continue;
                }
                b.sent = true;
            }


            int[] bt = {
                    b.x,
                    b.y,
                    b.getDirection(),
                    b.isDestroyed()?1:0,
                    b.id,
                    b.svrKill?1:0,
                    b.explode?1:0,
                    b.launched?1:0
            };
            bullets.add(bt);
        }
    }


    public MTank(Enemy e) {
        x = e.x;
        y = e.y;
        sx = e.sx;
        sy = e.sy;
        type = e.type;
        svrKill = e.svrKill;
        dirction = e.getDirection();
        tDestroyed = e.isDestroyed();
        boat = e.hasBoat();
//        shield = p.hasShield();
//        armour = p.armour;
        lives = Enemy.lives;
        typeVal = e.typeVal;
        group = e.group;
        respawn = e.respawn;
        id = e.id;
        hasBonus = e.hasBonus;




//        bullets = new ArrayList<>();
//        ArrayList<Bullet> pBullets = e.getBullets();
//        for(Bullet b:pBullets) {
//            if(b == null) {
//                continue;
//            }
////            if(b.isDestroyed() && !b.svrKill && WifiDirectManager.getInstance().isServer()) {
////                continue;
////            }
////
////            if(b.isDestroyed() && b.svrKill && !WifiDirectManager.getInstance().isServer()) {
////                continue;
////            }
//
//            int[] bt = {b.x,
//                        b.y,
//                        b.getDirection(),
//                        b.isDestroyed()?1:0,
//                        b.id,
//                        b.svrKill?1:0,
//                        b.explode?1:0,
//                        b.launched?1:0};
//            bullets.add(bt);
//        }
    }

    public MTank(HVE e) {
        x = e.x;
        y = e.y;
        v = e.getV();
        bv = e.getBV();
        sx = e.sx;
        sy = e.sy;
        hve = true;
        hits = e.getHits();
        type = e.type;
        svrKill = e.svrKill;
        dirction = e.getDirection();
        tDestroyed = e.isDestroyed();
        bombed = e.isBombed();
        boat = e.hasBoat();
//        shield = p.hasShield();
//        armour = p.armour;
        lives = Enemy.lives;
        typeVal = e.typeVal;
        group = e.group;
        respawn = e.respawn;
        id = e.id;
        hasBonus = e.hasBonus;




//        bullets = new ArrayList<>();
//        ArrayList<Bullet> pBullets = e.getBullets();
//        for(Bullet b:pBullets) {
//            if(b == null) {
//                continue;
//            }
////            if(b.isDestroyed() && !b.svrKill && WifiDirectManager.getInstance().isServer()) {
////                continue;
////            }
////
////            if(b.isDestroyed() && b.svrKill && !WifiDirectManager.getInstance().isServer()) {
////                continue;
////            }
//
//            int[] bt = {
//                    b.x,
//                    b.y,
//                    b.getDirection(),
//                    b.isDestroyed()?1:0,
//                    b.id,
//                    b.svrKill?1:0,
//                    b.explode?1:0,
//                    b.launched?1:0};
//            bullets.add(bt);
//        }
    }
}
