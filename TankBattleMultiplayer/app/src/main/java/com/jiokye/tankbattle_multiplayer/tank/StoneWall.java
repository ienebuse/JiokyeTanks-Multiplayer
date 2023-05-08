package com.jiokye.tankbattle_multiplayer.tank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

public class StoneWall extends GameObjects {
    Sprite sprite;
    Bitmap bitmap;
    boolean active = true;
    int onTmr = 3;
    boolean on = true;
    int frameCount = 0;
    int posx, posy;

    public StoneWall(int x, int y) {
        super(x, y);

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_STONE_WALL);
        bitmap = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y ,sprite.w,sprite.h);
        bitmap = Bitmap.createBitmap(bitmap,0,0,sprite.w,sprite.h);
        super.w = sprite.w;
        super.h = sprite.h;
        super.x = x*sprite.w;
        super.y = y*sprite.h;
        posx = x;
        posy = y;
    }

    public void collidsWithBullet(Bullet bullet) {
        if(isDestroyed()) {
            return;
        }
        if(super.collides_with(bullet)) {
//            setDestroyed();
            bullet.setDestroyed();
        }
    }

    public Point getPos() {
        return new Point(posx,posy);
    }

    public StoneWall setActive(boolean active) {
        this.active = active;
        if(!active) {

        }
        return this;
    }

    public void draw(Canvas canvas) {
        if(!active) {
            if(on){
                canvas.drawBitmap(bitmap,x,y,null);
            }
            --frameCount;
            if(frameCount <= 0){
                frameCount = onTmr;
                on = !on;
            }
        }
        else {
            canvas.drawBitmap(bitmap, x, y, null);
        }
    }
}
