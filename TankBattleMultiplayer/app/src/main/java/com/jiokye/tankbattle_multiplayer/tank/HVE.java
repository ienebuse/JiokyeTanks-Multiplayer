package com.jiokye.tankbattle_multiplayer.tank;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.model.MTank;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.util.ArrayList;

public class HVE extends Enemy{

    private  int frame_time = 7;
    private final int MAX_LIFE = 20;
    private int boostShield = MAX_LIFE;
    private ArrayList<Point> hView1, hView2;
    private boolean spawned = false;
    int view_frame_delay;
    int view_frame_time = 8;
    int view_frame;
    Paint vPaint;
    private static boolean viewing = false;
    private boolean gotTarget = false;
    private boolean bombed = false;
    public boolean bombSet = false;
    int dframe = 0;
    int dframe_delay;
    static boolean IS_AVAILABLE = false;
    private int hits = 0;
    private int mhits = 0;
    private float v,bv;


    public HVE(int x, int y, float v, float bv) {
        super(ObjectType.ST_TANK_D, 1, x, y );
//        nxtId++;
//        id = nxtId;
        hve = true;
        this.vx = DEFAULT_SPEED*v;//1.1f;
        this.vy = DEFAULT_SPEED*v;//1.1f;
        this.v = v;
        this.bv = bv;
        bulletSpeed = bv;//1.4f;
        MaxBullet = 1;
        reloadTmr = (int)(0.5*TankView.FPS);
        view_frame_delay = view_frame_time;
        view_frame = 0;
        vPaint = new Paint();
        vPaint.setColor(Color.GREEN);
        killScore = "600";

        if(!IS_AVAILABLE){
            SoundManager.stopSound(TankView.SCENE_SOUND);
            TankView.SCENE_SOUND = Sounds.TANK.HVE_SOUND;
            IS_AVAILABLE = true;
        }
        SoundManager.playSound(TankView.SCENE_SOUND,true);

    }

    public HVE(int x, int y, float v, float bv, int eid) {
        super(ObjectType.ST_TANK_D, 1, x, y, eid );
//        nxtId++;
//        id = nxtId;
        hve = true;
        this.vx = DEFAULT_SPEED*v;//1.1f;
        this.vy = DEFAULT_SPEED*v;//1.1f;
        this.v = v;
        this.bv = bv;
        bulletSpeed = bv;//1.4f;
        MaxBullet = 1;
        reloadTmr = (int)(0.5*TankView.FPS);
        view_frame_delay = view_frame_time;
        view_frame = 0;
        vPaint = new Paint();
        vPaint.setColor(Color.GREEN);
        killScore = "600";

        if(!IS_AVAILABLE){
            SoundManager.stopSound(TankView.SCENE_SOUND);
            TankView.SCENE_SOUND = Sounds.TANK.HVE_SOUND;
            IS_AVAILABLE = true;
        }
        SoundManager.playSound(TankView.SCENE_SOUND,true);

    }

    public float getV() {
        return this.v;
    }

    public float getBV() {
        return this.bv;
    }

    public boolean isBombed() {
        return bombSet;
    }

    public boolean hasTarget() {
        return gotTarget;
    }

    public void changeDirection() {
        if(TankView.freeze) {
            return;
        }
        /***
         * get target x,y
         *
         * get all x where there's possible movement in y towards target
         *
         * select x with the closest having the highest possibility for selection
         *
         * increase fire rate if aligned with target
         */
        if(dir_time >= change_dir_time) {
            dir_time = 0;
            change_dir_time = (int)(Math.random()*30 + 10);
            int new_direction;

            float d = (float)Math.random();
            if(d < 0.9 && target.x > 0 && target.y > 0) {
                int dx = (int)(target.x - x);
                int dy = (int)(target.y - y);

                d = (float)Math.random();

                if(Math.abs(dx) > Math.abs(dy)) {
                    new_direction = (d < 0.8) ? (dx < 0 ? CONST.Direction.LEFT : CONST.Direction.RIGHT) : (dy < 0 ? CONST.Direction.UP : CONST.Direction.DOWN);
                }
                else {
                    new_direction = (d < 0.8) ? (dy < 0 ? CONST.Direction.UP : CONST.Direction.DOWN) : (dx < 0 ? CONST.Direction.LEFT : CONST.Direction.RIGHT);
                }
            }
            else {
                new_direction = (int)(Math.random()*4)%4;
            }

            if(new_direction != direction) {
                direction = new_direction;

                int px_tile = (int)((x/tile_x)*tile_x);
                int py_tile = (int)((y/tile_y)*tile_y);

                if(x-px_tile < tile_x/TileScale) x = px_tile;
                else if(px_tile + tile_x - x < tile_x/TileScale) x = px_tile+tile_x;

                if(y-py_tile < tile_y/TileScale) y = py_tile;
                else if(py_tile + tile_y - y < tile_y/TileScale) y = py_tile+tile_y;
            }

            setStopPoint(change_dir_time, direction);
        }
        else {
            dir_time++;
        }
    }

//    public void setDestroyed() {
//        super.setDestroyed();
//        dframe = 0;
//        dframe_delay = dsprite.frame_time;
//    }

    public boolean collidsWithBullet(Bullet bullet) {
        if(isDestroyed() || respawn) {
            return false;
        }
        if(super.collides_with(bullet)) {
            TankView.getInstance().updateP1Hit();
            if(boat && TankView.ENEMY_BOOST) {
                boat = false;
                bullet.setDestroyed(false);
                return true;
            }
            if(hasBonus) {
                TankView.bonus.setBonus();
            }
            bullet.setDestroyed();
            ++hits;
            boostShield = MAX_LIFE - (hits + mhits);
            if(boostShield <= 0) {
                killed = true;
//                svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
                svrKill = TankView.twoPlayers && bullet.fromPlayer() == 1;
                setDestroyed();
                return true;
            }
            else {
//                --boostShield;
                SoundManager.playSound(Sounds.TANK.STEEL);
                return false;
            }
        }
        return false;
    }

    /**
     * Registers the bomb to ensures that the explosion kills enemy only
     * the first time
     * @param id - id of the bomb
     * @return returns true if this is the first encounter with the explosion, false otherwise
     */
    public boolean collideBomb(int id) {
        if(id == bombID){
            return false;
        }
        bombID = id;
//        boostShield -= 5;
        reduceShiled(5);
//        svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
        if(boostShield <= 0) {
            setDestroyed();
            return true;
        }
        return false;
    }

    public int getShiled() {
        return boostShield;
    }

    public int getHits() {
        return hits;
    }

    public int reduceShiled(int amount) {
        hits += amount;
        boostShield = MAX_LIFE - (hits + mhits);
        svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
//        boostShield -= amount;

        if(boostShield > 0) {
            bombed = true;
            bombSet = true;
            dframe = 0;
            dframe_delay = dsprite.frame_time;
            SoundManager.playSound(Sounds.TANK.EXPLOSION, 1.5f,4);
        }
        return boostShield;
    }

    public ArrayList<Point> getPlayerView(Player p) {
        ArrayList<Point> hView = new ArrayList<>();
        int px, py;
        int D = (int)Math.sqrt(Math.pow(p.y - y,2) + Math.pow(p.x - x,2));
        double angle = Math.atan2(p.y - y, p.x - x);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        int d = tile_x;
        while (d<D) {
            py = (int)(d*sinAngle + y+h/2);
            px = (int)(d*cosAngle + x+w/2f);
            hView.add(new Point(px,py));
            d += tile_x;
        }
        return hView;
    }

    public void getView(Player p) {
        hView1 = getPlayerView(p);
        gotTarget = true;
        viewing = true;
    }

    public void getView(Player p1, Player p2) {
        hView1 = p1 != null ? getPlayerView(p1) : null;
        hView2 = p2 != null ? getPlayerView(p2) : null;
        gotTarget = true;
        viewing = true;
//        Log.d("HVE","Got view");
    }

    public static boolean isViewing() {
        return viewing;
    }

    public void setModel(MTank model, float scale, boolean server) {
        if(!server){
            if(this.direction != model.dirction) {
                this.direction = model.dirction;
                this.x = (int) (model.x * scale);
                this.y = (int) (model.y * scale);
            }

            this.sx = (int) (model.sx * scale);
            this.sy = (int) (model.sy * scale);

            this.setBoat(model.boat);
            lives = Math.min(lives,model.lives);
            if(model.group < this.group){
                this.group = model.group;
            }
            this.typeVal = model.typeVal;
            switch (typeVal) {
                case 0:
                    type = ObjectType.ST_TANK_A;
                    break;
                case 1:
                    type = ObjectType.ST_TANK_B;
                    break;
                case 2:
                    type = ObjectType.ST_TANK_C;
                    break;
                case 3:
                    type = ObjectType.ST_TANK_D;
                    break;
            }
//            this.respawn = model.respawn;
            this.id = model.id;
            this.hasBonus = model.hasBonus;

            if(model.bombed && !bombSet && model.svrKill) {
                bombed = true;
                dframe = 0;
                dframe_delay = dsprite.frame_time;
            }

            if (model.tDestroyed && !destroyed && model.svrKill) {
                setDestroyed();
                svrKill = model.svrKill;
            }

            ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TankView.setEnemyCountView();

                }
            });

        }
        else {
            if(model.group < group){
                group = model.group;
            }

            this.setBoat(model.boat);

            if(model.bombed && !bombSet && !model.svrKill) {
                bombed = true;
                dframe = 0;
                dframe_delay = dsprite.frame_time;
            }

            if (model.tDestroyed && !destroyed && !model.svrKill) {
                setDestroyed();
                svrKill = model.svrKill;
            }
        }

        boostShield = MAX_LIFE - (hits + model.hits);
        mhits = model.hits;
    }

    public void draw(Canvas canvas) {
        if(respawn) {
            if(frame >= spsprite.frame_count) {
                respawn = false;
                spawned = true;
                //todo -- commented to stop HVE viewing
                viewing = true;
                frame = 0;
//                Log.d("HVE", "Respawn ended");
                return;
            }
            canvas.drawBitmap(spbitmap[frame],x,y,null);
            if(frame_delay <= 0) {
                frame++;
                frame_delay = spsprite.frame_time;
            }
            else{
                --frame_delay;
            }
        }
        else if(!destroyed) {

            frame %= TankView.hveSprite.frame_count;//sprite.frame_count;
            int l = (int)(((float)boostShield/MAX_LIFE)*w);
            canvas.drawLine(x,y-(h/8),x+l, y-h/8,vPaint);
//            canvas.drawBitmap(TankView.tankBitmap.get(sprite.frame_count * typeVal + frame).get(4*lifeFrame+direction),x,y,null);
            canvas.drawBitmap(TankView.hveBitmap.get(frame).get(direction),x,y,null);
            if(frame_delay <= 0) {
                int mod = (int)Math.ceil(boostShield/4);
                frame = (frame + 1) % TankView.hveSprite.frame_count;
                frame_delay = frame_time;//sprite.frame_time;
                lifeFrame = (lifeFrame + 1) % 5;
            }
            else{
                --frame_delay;
            }
            if(boat) {
                mBoat.setPosition(x,y);
                mBoat.draw(canvas);
            }

            if(shield && shieldTmr > 0) {
                mShield.setPosition(x,y);
                mShield.draw(canvas);
            }

            // todo -- commented to stop HVE viewing

            if(spawned) {
                int len = 0;
                if(TankView.twoPlayers) {
                    len = Math.max(hView1 != null?hView1.size():0,hView2 != null?hView2.size():0);
                }
                else {
                    len = hView1.size();
                }
                for(int i = 0; i < len; i++) {
                    if(TankView.twoPlayers) {
                        if(hView1 != null && i < hView1.size()) {
                            canvas.drawCircle(hView1.get(i).x, hView1.get(i).y, 2, vPaint);
                        }
                        if(hView2 != null && i < hView2.size()) {
                            canvas.drawCircle(hView2.get(i).x, hView2.get(i).y, 2, vPaint);
                        }
                    }
                    else {
                        canvas.drawCircle(hView1.get(i).x, hView1.get(i).y, 2, vPaint);
                    }
                    if(i == view_frame) {
                        break;
                    }
                }
                if(view_frame_delay <= 0){
                    view_frame_delay = view_frame_time;
                    view_frame++;
                }
                else {
                    --view_frame_delay;
                }
                if(view_frame >= len) {
                    spawned = false;
                    viewing = false;
//                    Log.d("HVE", "Finished viewing " + len);
                }
            }

            if(bombed) {
                if (dframe < dsprite.frame_count) {
                    canvas.drawBitmap(dbitmap[dframe], x - (int) (w / 2), y - (int) (h / 2), null);
                    if(dframe_delay <= 0) {
                        dframe = dframe + 1;
                        dframe_delay = dsprite.frame_time;
                    }
                    else{
                        --dframe_delay;
                    }
                } else if (dframe == dsprite.frame_count) {
                    bombed = false;
//                respawn();
                }
            }
        }
        else if(!recycle) {
            if (frame < dsprite.frame_count) {
                canvas.drawBitmap(dbitmap[frame], x - (int) (w / 2), y - (int) (h / 2), null);
                if(killed){
                    drawText(canvas,killScore);
                }
                if(frame_delay <= 0) {
                    frame = frame + 1;
                    frame_delay = dsprite.frame_time;
                }
                else{
                    --frame_delay;
                }
            } else if (frame == dsprite.frame_count) {
                killed = false;
                super.recycle();
                if(!TankView.twoPlayers || WifiDirectManager.getInstance().isServer()) {
                    EnemyCount--;
                }
//                respawn();
            }
        }

//        for(Bullet bullet:bullets) {
//            if(bullet != null) {
//                bullet.draw(canvas);
//            }
//        }
    }
}
