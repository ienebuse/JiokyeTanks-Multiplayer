package com.jiokye.tankbattle_multiplayer.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CONST;

public class Mine extends GameObjects {
    Sprite sprite;
    Bitmap[] bitmap;
    int frame_delay = 0;
    int frame = 0;
    boolean explode = false;
    int fuseTime = 4*TankView.FPS;
    int fuseTmr;
//    Rect[] explodeRect = {new Rect(), new Rect(), new Rect(), new Rect(), new Rect()};
    Rect[] explodeRect = {new Rect(), new Rect(), new Rect(), new Rect(), new Rect(), new Rect(), new Rect(), new Rect(), new Rect()};
    boolean from_player;
    public int id = 0;
    private boolean dropped = false;
    boolean up = true;
    private long activateTime;
    private boolean moving = false;
    float acc,v;
    int dir;
//    float x, y;

    private final int
            MIDDLE = 0,
            UP = 1,
            DOWN = 2,
            RIGHT = 3,
            LEFT = 4;


    public Mine(int x, int y, boolean from_player) {
        super(x, y);
//        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_WATER);
//        Bitmap bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y ,sprite.w,sprite.h*2);
//        bitmap = new Bitmap[2];
//        for(int i = 0; i < 2; i++){
//            bitmap[i] = Bitmap.createBitmap(bm,0,i*sprite.h, sprite.w, sprite.h);
//        }
        super.w = TankView.bombSprite.w;
        super.h = TankView.bombSprite.h;
        this.from_player = from_player;
        x = x;
        y = y;



        fuseTmr = fuseTime;
        acc = -(float)TankView.tile_dim/40;
        v = (float)TankView.tile_dim/2;


//        super.x = x*sprite.w;
//        super.y = y*sprite.h;
    }

    public void setPosition(int xp, int yp) {
        this.x = xp+w/2;
        this.y = yp+h/2;
//        x = xp;// + w/2;
//        y = yp;// + h/2;



    }

    public boolean isMoving() {
        return moving;
    }

    public void drop(int dir) {
        ++id;
        dropped = true;
        moving = true;
        explode = false;
        this.dir = dir;
        v = TankView.tile_dim/2f;
        fuseTmr = fuseTime;
        frame = 0;
        frame_delay = TankView.bombSprite.frame_time;
    }

    public void activate() {
        activateTime = System.currentTimeMillis();
    }

    public void setDestroyed() {
        dropped = false;
    }

    public void setExplosion() {
        frame = 0;
        explode = true;
        dropped = false;
        frame_delay = TankView.fireSprite.frame_time;
        up = true;
        int MID = 4;

        int dim = TankView.fireSprite.w;
        int dimx = dim;

//        int x = this.x+w/4;
//        int y = this.y+h/4;

        explodeRect[MIDDLE].left = (int)x;
        explodeRect[MIDDLE].top = (int)y;
        explodeRect[MIDDLE].right = (int)x+dim;
        explodeRect[MIDDLE].bottom = (int)y+dim;

        explodeRect[UP].left = (int)x;
        explodeRect[UP].top = Math.max((int)y - dim - dimx, 0);
        explodeRect[UP].right = (int)x+dim;
        explodeRect[UP].bottom = (int)y - dimx;

        explodeRect[RIGHT].left = (int)x+dim + dimx;
        explodeRect[RIGHT].top = (int)y;
        explodeRect[RIGHT].right = Math.min((int)x + 2*dim + dimx, TankView.WIDTH);
        explodeRect[RIGHT].bottom = (int)y+dim;

        explodeRect[DOWN].left = (int)x;
        explodeRect[DOWN].top = (int)y+dim + dimx;
        explodeRect[DOWN].right = (int)x+dim;
        explodeRect[DOWN].bottom = Math.min((int)y + 2*dim + dimx, TankView.HEIGHT);

        explodeRect[LEFT].left = Math.max((int)x - dim - dimx, 0);
        explodeRect[LEFT].top = (int)y;
        explodeRect[LEFT].right = (int)x - dimx;
        explodeRect[LEFT].bottom = (int)y+dim;


        explodeRect[UP+MID].left = (int)x;
        explodeRect[UP+MID].top = Math.max((int)y - dim, 0);
        explodeRect[UP+MID].right = (int)x+dim;
        explodeRect[UP+MID].bottom = (int)y;

        explodeRect[RIGHT+MID].left = (int)x+dim;
        explodeRect[RIGHT+MID].top = (int)y;
        explodeRect[RIGHT+MID].right = Math.min((int)x + 2*dim, TankView.WIDTH);
        explodeRect[RIGHT+MID].bottom = (int)y+dim;

        explodeRect[DOWN+MID].left = (int)x;
        explodeRect[DOWN+MID].top = (int)y+dim;
        explodeRect[DOWN+MID].right = (int)x+dim;
        explodeRect[DOWN+MID].bottom = Math.min((int)y + 2*dim, TankView.HEIGHT);

        explodeRect[LEFT+MID].left = Math.max((int)x - dim, 0);
        explodeRect[LEFT+MID].top = (int)y;
        explodeRect[LEFT+MID].right = (int)x;
        explodeRect[LEFT+MID].bottom = (int)y+dim;
//        setPosition(x, y);
    }

    public boolean destroyObj(GameObjects obj) {
        for(Rect b:explodeRect) {
            if(Rect.intersects(b,obj.getRect())){
//                Log.d("BOMB COLLISION", "Got Collision");
                return true;
            }
        }
//        Log.d("BOMB COLLISION", "Got no Collision");
        return false;
    }

    protected boolean collides_with(GameObjects targ) {
        rect = getRect();
        Rect r = new Rect(rect);
        int offset = Math.max(1,TankView.tile_dim/4);

        r.left -= offset;
        r.right += offset;
        r.bottom += offset;
        r.top -= offset;
        Rect trect = new Rect(targ.getRect());
        trect.left -= offset;
        trect.right += offset;
        trect.bottom += offset;
        trect.top -= offset;

        return Rect.intersects(r,trect);
    }

    public boolean collidesWithObject(GameObjects obj) {
        x = (int)x;
        y = (int)y;
        boolean checkObj = collides_with(obj);
        boolean checkWall = super.collides_with_wall();


        if(checkObj || checkWall) {
            moving = false;
            switch (dir) {
                case CONST.Direction.UP:
                case CONST.Direction.DOWN:
                    y = (int)(Math.floor(y/26.0 + 0.5))*26;
                    break;
                case CONST.Direction.LEFT:
                case CONST.Direction.RIGHT:
                    x = (int)(Math.floor(x/26.0 + 0.5))*26;
                    break;
            }
//            x = (int)(Math.floor(x/26.0 + 0.5))*26;
//            y = (int)(Math.floor(y/26.0 + 0.5))*26;
            moving = false;
            x = (int)x;
            y = (int)y;

            if(checkObj && (obj instanceof Player || obj instanceof Enemy)) {
                if(fuseTmr > 2) {
                    fuseTmr = 2;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isExploding() {
        return explode;
    }

    public boolean isDropped() {
        return dropped;
    }



    public void update() {
        if(dropped && fuseTmr > 0) {
            --fuseTmr;
            if(fuseTmr == 0) {
//                Log.d("PLAYER BOMB", "Explosion");
                setExplosion();
                SoundManager.playSound(Sounds.TANK.BOMB);
            }
        }

        if(moving) {
            switch (dir) {
                case CONST.Direction.UP:
                    y -= v;
                    if(y < 0) {
                        y = 0;
                        moving = false;
                    }
                    break;
                case CONST.Direction.DOWN:
                    y += v;
                    if(y > TankView.HEIGHT - h) {
                        y = TankView.HEIGHT - h;
                        moving = false;
                    }
                    break;
                case CONST.Direction.LEFT:
                    x -= v;
                    if(x < 0) {
                    x = 0;
                    moving = false;
                }
                    break;
                case CONST.Direction.RIGHT:
                    x += v;
                    if(x > TankView.WIDTH - w) {
                        x = TankView.WIDTH - w;
                        moving = false;
                    }
                    break;
            }
//            Log.d("BOMB", String.valueOf(x)+" "+y+" "+acc);
            v += acc;
            if(v <= 0){
                moving = false;
            }
        }
    }

    public void draw(Canvas canvas) {
        if(dropped) {
            if (frame_delay <= 0) {
                frame = (frame + 1) % TankView.bombSprite.frame_count;
                frame_delay = TankView.bombSprite.frame_time;
            } else {
                --frame_delay;
            }
            canvas.drawBitmap(TankView.bombBitmap.get(frame), x, y, null);
        }
        else if(explode) {
//            Log.d("BOMB", String.valueOf(frame)+" "+ dropped +" "+ explode +" "+ up);
            if(frame < 0) {
                dropped = false;
                explode = false;
                return;
            }

            for(int dir = 0; dir < 9; dir++) {
                int d;
                if(dir < 5) {
                    d = dir;
                }
                else if(dir == 7 || dir == 8) {
                    d = 5;
                }
                else {
                    d = 6;
                }
                canvas.drawBitmap(TankView.fireBitmap[d][frame], explodeRect[dir].left, explodeRect[dir].top, null);
            }

            if(frame_delay <= 0) {
                if(up) {
                    frame++;
                    if(frame >= TankView.fireSprite.frame_count) {
                        up = false;
                        frame = TankView.fireSprite.frame_count - 1;
                    }
                }
                else{
                    frame--;
                }
                frame_delay = TankView.fireSprite.frame_time;
            }
            else {
                --frame_delay;
            }



//            canvas.drawBitmap(TankView.fireBitmap[MIDDLE][frame], explodeRect[MIDDLE].left, explodeRect[MIDDLE].top, null);
//            canvas.drawBitmap(TankView.fireBitmap[UP][frame], explodeRect[UP].left, explodeRect[UP].top, null);
//            canvas.drawBitmap(TankView.fireBitmap[DOWN][frame], explodeRect[DOWN].left, explodeRect[DOWN].top, null);
//            canvas.drawBitmap(TankView.fireBitmap[RIGHT][frame], explodeRect[RIGHT].left, explodeRect[RIGHT].top, null);
//            canvas.drawBitmap(TankView.fireBitmap[LEFT][frame], explodeRect[LEFT].left, explodeRect[LEFT].top, null);
//            canvas.drawBitmap(TankView.fireBitmap[5][frame], x, y, null);
//            canvas.drawBitmap(TankView.fireBitmap[6][frame], x, y, null);

//            explode = false;
//            setDestroyed();
        }

    }
}
