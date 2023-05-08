package com.jiokye.tankbattle_multiplayer.tank;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.jiokye.tankbattle_multiplayer.activity.TankActivity;
import com.jiokye.tankbattle_multiplayer.model.MTank;
import com.jiokye.tankbattle_multiplayer.sound.SoundManager;
import com.jiokye.tankbattle_multiplayer.sound.Sounds;
import com.jiokye.tankbattle_multiplayer.utility.CONST;
import com.jiokye.tankbattle_multiplayer.wifidirect.WifiDirectManager;

import java.util.ArrayList;

public class Player extends Tank{

    public int armour = 0;
    protected int frame;
    protected int frame_delay;
    public int lives = 3;
    private int mines = 3;
    private int builders = 3;
    private int buildDir = 0;
    protected int reloadTmr = (int)(0.1*TankView.FPS);
    protected int reload_time = 0;
    protected int MaxBullet = 1;
    protected int starCount = 0;
//    protected int direction;
    private int newDirection;
    private boolean clearBush = false;
    private boolean breakWall = false;
    private int level = 0;
    private  float bulletSpeed = 1;
    private boolean boat = false;
    public int[] kills;
    public int totalKills;
    public int totalScore;
    public int stageScore;
    boolean killed = false;
    public int gotBonus = 0;
    private boolean freezeOn = false;
    private int freezeOnTmr = 0, freezBlinkTime = 5;
    public int bulletIntercept = 0;
    public ArrayList<Long> killTime = new ArrayList<>();
    public int bonusID = -10;
    private boolean stop_moving = true;
    private boolean stop_shooting = true;
    int bombID = -1;
    int bombID2 = -1;
    int hit;
    int bulletCount;

    GameObjects drop1,drop2;
    GameObjects drop1b,drop2b;
    Point p1 = new Point(), p2 = new Point();
    boolean dropActive = false;






    public Player(ObjectType type, int x, int y, int player) {
        super(type, x, y, player);

        direction = CONST.Direction.UP;

        if(type == ObjectType.ST_PLAYER_1) {
            this.group = 5;
            super.x = (int)(4*(TankView.WIDTH/13));
            super.y = TankView.HEIGHT - h;
        }
        else {
            this.group = 6;
            super.x = (int)(8*(TankView.WIDTH/13));
            super.y = TankView.HEIGHT - h;
        }
        bulletSpeed = 1;
        shield = true;
        shieldTmr = ShieldTime/2;
        iceTmr = 0;
        kills = new int[]{0,0,0,0};
        if (this.type == ObjectType.ST_PLAYER_1) {
            ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(lives));
        } else {
            ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(lives));
        }
        changeDirection(direction);
        totalScore = 0;
        stageScore = 0;
        totalKills = 0;
        frame = 0;
        hit = 0;
        bulletCount = 0;
    }

    public void move() {
//        Log.d("MOTION", String.valueOf(moving) + " " + collision + " " + direction);
        if(respawn || destroyed || stop_moving) {
            return;
        }

        if((moving && !collision) || iceTmr > 0){
            getRect();
            if(freeze && freezeTmr > 0) {
                --freezeTmr;
                return;
            }
            SoundManager.playSound(Sounds.TANK.BACKGROUND,0.1f, 0);
            switch (direction) {
                case CONST.Direction.UP:
                    if(y <= 0)return;
                    y -= vy;
                    if(y < 0){
                        y = 0;
                    }
                    break;
                case CONST.Direction.DOWN:
                    if(y >= TankView.HEIGHT - h)return;
                    y += vy;
                    if(y > TankView.HEIGHT - h){
                        y = TankView.HEIGHT - h;
                    }

                    break;
                case CONST.Direction.LEFT:
                    if(x <= 0)return;
                    x -= vx;
                    if(x < 0) {
                        x = 0;
                    }
                    break;
                case CONST.Direction.RIGHT:
                    if(x >= TankView.WIDTH - w)return;
                    x += vx;
                    if(x > TankView.WIDTH - w) {
                        x = TankView.WIDTH - w;
                    }
                    break;
            }

        }
    }

    public void updateHit() {
        hit++;
    }

    public float getHitRate() {
        return (float)hit/bulletCount;
    }

    public void resetHit() {
        hit = 0;
        bulletCount = 0;
    }

    public boolean canClearBush() {
        return clearBush;
    }

    public boolean canBreakWall() {
        return breakWall;
    }

    public boolean hasBoat () {
        return boat;
    }

    public void setBoat(boolean boat) {
        this.boat = boat;
    }

    public boolean hasShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
        this.shieldTmr = ShieldTime;
    }

    public int getDirection() {
        return direction;
    }

    public void move(int dir) {
        if(stop_moving) {
            return;
        }
        if(iceTmr > 0) {
            newDirection = dir;
            moving = true;
            return;
        }
        if(dir != direction) {
            changeDirection(dir);
        }

        moving = true;

        int[] r = setCollissionRect(direction);
    }

    public void resetKills() {
        kills = new int[] {0,0,0,0};
        totalKills = 0;
    }

    public void changeDirection(int dir) {
        if(freeze) {
            return;
        }
        if(iceTmr > 0) {
            newDirection = dir;
            return;
        }
        direction = dir;
        newDirection = dir;
        int px_tile = (int) ((x / tile_x) * tile_x);
        int py_tile = (int) ((y / tile_y) * tile_y);

        if (x - px_tile < tile_x / TileScale) x = px_tile;
        else if (px_tile + tile_x - x < tile_x / TileScale) x = px_tile + tile_x;

        if (y - py_tile < tile_y / TileScale) y = py_tile;
        else if (py_tile + tile_y - y < tile_y / TileScale) y = py_tile + tile_y;
    }

    public boolean collidesWithObject(GameObjects targ) {
        rect = getRect();
        Rect tRect = targ.getRect();
        setCollissionRect(direction);
        if(Rect.intersects(cRct,tRect)) {
            collision = true;
        }
        else {
            collision = false;
        }
        return  collision;
    }



    public void stopMoving() {
        moving = false;
    }

    public void startMoving() {
        moving = true;
    }

    public void disableMove() {
        stop_moving = true;
    }

    public void enableMove() {
        stop_moving = false;
    }

    public void disableFire() {
        stop_shooting = true;
    }

    public void enableFire() {
        stop_shooting = false;
    }

    public void fire() {
//        Log.d("Tile", tile_x+" "+tile_y);
        if(!shooting|| stop_shooting || respawn || isDestroyed()) {
            return;
        }
        if(bullets.size() == MaxBullet) {
            return;
        }
        if(MaxBullet > 1 && reload_time > 0) {
            return;
        }

        int bx=0,by=0;
        switch (direction) {
            case CONST.Direction.UP:
                bx = x+(int) sprite.w/2;
                by = y;
                by = (int) ((by / tile_y) * tile_y) + tile_y;
                break;
            case CONST.Direction.DOWN:
                bx = x+(int) sprite.w/2;
                by = y+ sprite.h;
                by = (int) ((by / tile_y) * tile_y) - tile_y;
                break;
            case CONST.Direction.LEFT:
                bx = x;
                bx = (int) ((bx / tile_x) * tile_x) + tile_x;
                by = y+(int) sprite.h/2;
                break;
            case CONST.Direction.RIGHT:
                bx = x+ sprite.w;
                bx = (int) ((bx / tile_x) * tile_x) - tile_x;
                by = y+(int) sprite.h/2;
                break;
        }
        bId++;
        bullet.move(direction);
        bullet.setSpeed(bulletSpeed);
        bullet.setPlayer(this.player);
        bullet.id = bId;
        //todo add player bullet  to tank game bullets
        bullets.add((new Bullet(bullet,bx,by)));
        bulletCount++;
//        activeGameBullets.add(new Bullet(bullet,bx,by));
        SoundManager.playSound(Sounds.TANK.FIRE);
        if(MaxBullet > 1){
            reload_time = reloadTmr;
        }
    }

    public void loseLife() {
        --lives;
        if(lives <= 0) {
            lives = 0;
            ((TankActivity) (TankView.getInstance().getTankViewContext())).disableBonusStack();
        }

//        if(lives <= 1) {
//            ((TankActivity) (TankView.getInstance().getTankViewContext())).disableGift();
//        }

        updateLifeView();
    }

    public void updateLifeView() {
        ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Player.this.type == ObjectType.ST_PLAYER_1) {
                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(Player.this.lives));
                } else {
                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(Player.this.lives));
                }

            }
        });
    }


    public void updateMineView() {
        ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (Player.this.player == 1) {
                    ((TankActivity) (TankView.getInstance().getTankViewContext())).bmbText.setText(String.valueOf(Player.this.mines));
//                }
            }
        });
    }

    public boolean isAlive() {
        return lives > 0;
    }

    public void upgrade_armour() {
        armour++;
    }

    public void setDestroyed() {
        super.setDestroyed();
        killed = true;
        TankView.vibrate(500);
    }

    public void setFreeze() {
        freeze = true;
//        TankView.freeze = true;
//        TankView.freezeTmr = TankView.FreezeTime;
    }


    public void freeze() {
        freeze = true;
        freezeTmr = FreezeTime;
        freezeOnTmr = 0;
    }

    public void unFreeze() {
        freeze = false;
    }

    public boolean isFrozen() {
        return freeze;
    }

    public void setBBomb(boolean bBomb) {
        this.bBomb = bBomb;
    }

    public boolean getBBomb() {
        return bBomb;
    }

    public void iceSlippage() {
        if(moving && !slip) {
            slip = true;
            iceTmr = IceTime;
            SoundManager.playSound(Sounds.TANK.SLIDE, 1, 3);
        }
    }

    public void stopSlip() {
        iceTmr = 0;
        slip = false;

        if(moving) {
            move(newDirection);
        }

    }

    public void applyShield() {
        shield = true;
        shieldTmr = ShieldTime;
    }

    public void applyBoat() {
        boat = true;
        boatTmr = BoatTime;
    }

    public void applyTank() {
        ++lives;
        if(lives > 1) {
            ((TankActivity) (TankView.getInstance().getTankViewContext())).enableGift();
        }

        updateLifeView();
//        ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(lives > 1) {
//                    ((TankActivity) (TankView.getInstance().getTankViewContext())).enableGift();
//                }
//
//                updateLifeView();
////                if (Player.this.type == ObjectType.ST_PLAYER_1) {
////                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(Player.this.lives));
////                } else {
////                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(Player.this.lives));
////                }
//            }
//        });

    }

    public void applyGun() {
        bulletSpeed = 1.3f;
        breakWall = true;
        vx *= 1.3;
        vy *= 1.3;
        if(vx > DEFAULT_SPEED*1.35){
            vx = DEFAULT_SPEED*1.35f;
        }
        if(vy > DEFAULT_SPEED*1.35){
            vy = DEFAULT_SPEED*1.35f;
        }
        starCount += 3;
        if(starCount > 3) {
            clearBush = true;
            starCount = 4;
        }
        MaxBullet = 2;
        armour = 3;
        level = 1;
    }

    public void applyStar() {
        ++starCount;
        if(starCount > 3) {
            clearBush = true;
            starCount = 4;
        }
        if(starCount >= 3) {
            breakWall = true;
        }
        bulletSpeed = 1.3f;
        if(starCount >= 2) {
            MaxBullet = 2;
        }
        armour++;
        vx *= 1.2;
        if(vx > DEFAULT_SPEED*1.35){
            vx = DEFAULT_SPEED*1.35f;
        }
        vy *= 1.2;
        if(vy > DEFAULT_SPEED*1.35){
            vy = DEFAULT_SPEED*1.35f;
        }
        if(armour >= 3){
            armour = 3;
            level = 1;
        }
    }

    public void applyMine() {
        ++mines;
        updateMineView();
    }

    public void dropMine() {
        if(mines <= 0 || lives <= 0 || getMine().isDropped()) {
            return;
        }
        super.dropMine();
        --mines;
//        if (Player.this.player == 1) {
            updateMineView();
//        }
    }

    public int getMineCounts() {
        return mines;
    }

    public int getBuildersCount() {
        return builders;
    }

    public void placeBuild() {
        Log.d("BUILDER", "Place build");
        if(dropActive  && drop1 != null && drop2 != null) {
            ((StoneWall)drop1).setActive(true);
            ((StoneWall)drop2).setActive(true);
            dropActive = false;
        }
    }

    public void dropBuild() {
        if(!dropActive){
            buildDir = CONST.Direction.UP;
            dropActive = true;
        }
        else{
            buildDir = (buildDir + 1)%4;
            TankView.getInstance().setLevelObjects(p1.x,p1.y, drop1b);
            TankView.getInstance().setLevelObjects(p2.x,p2.y, drop2b);
        }
        Log.d("BUILDER", "Drop build");
        int posx = (int)((float)2*x/w + 0.5);
        int posy = (int)((float)2*y/h + 0.5);
        switch (buildDir) {
            case CONST.Direction.UP:
                posy = Math.max(posy-1,0);
                drop1b = TankView.getInstance().getLevelObjects(posx,posy);
                drop2b = TankView.getInstance().getLevelObjects(posx+1,posy);
                p1.x = posx; p1.y = posy;
                p2.x = posx+1; p2.y = posy;

                drop1 = new StoneWall(posx,posy).setActive(false);
                drop2 = new StoneWall(posx+1,posy).setActive(false);
                TankView.getInstance().setLevelObjects(posx,posy, drop1);
                TankView.getInstance().setLevelObjects(posx+1,posy, drop2);
                break;
            case CONST.Direction.RIGHT:
                posx = Math.min(posx+2,25);
                drop1b = TankView.getInstance().getLevelObjects(posx,posy);
                drop2b = TankView.getInstance().getLevelObjects(posx,posy+1);
                p1.x = posx; p1.y = posy;
                p2.x = posx; p2.y = posy+1;

                drop1 = new StoneWall(posx,posy).setActive(false);
                drop2 = new StoneWall(posx,posy+1).setActive(false);
                TankView.getInstance().setLevelObjects(posx,posy, drop1);
                TankView.getInstance().setLevelObjects(posx,posy+1, drop2);
                break;
            case CONST.Direction.DOWN:
                posy = Math.min(posy+2,25);
                drop1b = TankView.getInstance().getLevelObjects(posx,posy);
                drop2b = TankView.getInstance().getLevelObjects(posx+1,posy);
                p1.x = posx; p1.y = posy;
                p2.x = posx+1; p2.y = posy;

                drop1 = new StoneWall(posx,posy).setActive(false);
                drop2 = new StoneWall(posx+1,posy).setActive(false);
                TankView.getInstance().setLevelObjects(posx,posy, drop1);
                TankView.getInstance().setLevelObjects(posx+1,posy, drop2);
                break;
            case CONST.Direction.LEFT:
                posx = Math.max(posx-1,0);
                drop1b = TankView.getInstance().getLevelObjects(posx,posy);
                drop2b = TankView.getInstance().getLevelObjects(posx,posy+1);
                p1.x = posx; p1.y = posy;
                p2.x = posx; p2.y = posy+1;

                drop1 = new StoneWall(posx,posy).setActive(false);
                drop2 = new StoneWall(posx,posy+1).setActive(false);
                TankView.getInstance().setLevelObjects(posx,posy, drop1);
                TankView.getInstance().setLevelObjects(posx,posy+1, drop2);
                break;
        }
    }

    public void activateBuild() {
        Log.d("BUILDER", "Activate build");

    }

    public Mine getMine() {
        return mine;
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
        svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
        setDestroyed();
        return true;
    }

    public boolean collideBomb2(int id) {
        if(id == bombID2){
            return false;
        }
        bombID2 = id;
        svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
        setDestroyed();
        return true;
    }

    public int collidsWithBonus(Bonus b) {
        if(b.isAvailable() && super.collides_with(b)) {
            if(bonusID == Bonus.id) {
                b.clearBonus();
                return -1;
            }
            bonusID = Bonus.id;
            TankView.getInstance().currentObj[3] = false;
            stageScore += 500;

            int bonus = b.getBonus();

            if(bonus == Bonus.TANK) {
                SoundManager.playSound(Sounds.TANK.BONUS1UP, 1, 3);
            }
            else {
                SoundManager.playSound(Sounds.TANK.BONUS, 1, 3);
            }
            gotBonus = bonus;
            switch (bonus) {
                case Bonus.GRENADE:
                    break;
                case Bonus.HELMET:
                    applyShield();
                    break;
                case Bonus.CLOCK:
                    break;
                case Bonus.SHOVEL:
                    break;
                case Bonus.TANK:
                    applyTank();
                    break;
                case Bonus.STAR:
                    applyStar();
                    break;
                case Bonus.GUN:
                    applyGun();
                    break;
                case Bonus.BOAT:
                    applyBoat();
                    break;
                case Bonus.MINE:
                    applyMine();
                    break;
            }
            if(player != 0) {
                b.clearBonus();
            }
            return bonus;
        }
        return -1;
    }

    public boolean collidsWithBullet(Bullet bullet) {
        if(isDestroyed()){
            return false;
        }
        if(super.collides_with(bullet)) {
//            if(TankView.twoPlayers && !WifiDirectManager.getInstance().isServer()) {
//                bullet.deactivate();
//                return true;
//            }
            TankView.getInstance().currentObj[0] = false;
            if(TankView.twoPlayers && bullet.fromPlayer() > 0) {
                bullet.recycle = true;
                return true;
            }

            if(shield) {
                bullet.setDestroyed(false);
                return true;
            }
            if(boat) {
                boat = false;
                bullet.setDestroyed(false);
                return true;
            }
            if(armour >= 3) {
                armour = 2;
                clearBush = false;
                breakWall = false;
                starCount = 2;
                bullet.setDestroyed();
                return true;
            }
            svrKill = TankView.twoPlayers && WifiDirectManager.getInstance().isServer();
//            svrKill = TankView.twoPlayers && this.player == 1;
            setDestroyed();
            bullet.setDestroyed();

            return true;
        }
        return false;
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public void respawn() {
        if(!killed) {
            respawn(!killed);
            killed = false;
            return;
        }
        respawn = true;
        killed = false;
        starCount = 0;
        armour = 0;
        boat = false;
        bulletSpeed = 1;
        clearBush = false;
        breakWall = false;
        destroyed = false;
        shield = true;
        vx = DEFAULT_SPEED;
        vy = DEFAULT_SPEED;
        MaxBullet = 1;
        shieldTmr = ShieldTime/2;
        direction = CONST.Direction.UP;
        if(this.type == ObjectType.ST_PLAYER_1) {
            x = (int) (4 * (TankView.WIDTH / 13));
        }
        else {
            x = (int)(8*(TankView.WIDTH/13));
        }
        y = TankView.HEIGHT - h;
        frame = 0;

        updateLifeView();

//        ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (Player.this.type == ObjectType.ST_PLAYER_1) {
//                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(Player.this.lives));
//                } else {
//                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(Player.this.lives));
//                }
//            }
//        });

        changeDirection(direction);
    }

    public void respawn(boolean restart) {
        respawn = true;
        destroyed = false;
        shield = true;
        shieldTmr = ShieldTime/2;
        direction = CONST.Direction.UP;
        if(this.type == ObjectType.ST_PLAYER_1) {
            x = (int) (4 * (TankView.WIDTH / 13));
        }
        else {
            x = (int)(8*(TankView.WIDTH/13));
        }
        y = TankView.HEIGHT - h;
        frame = 0;

        updateLifeView();

//        ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (Player.this.type == ObjectType.ST_PLAYER_1) {
//                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(Player.this.lives));
//                } else {
//                    ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(Player.this.lives));
//                }
//            }
//        });
        changeDirection(direction);
    }

    public void updateBullets() {
        for(Bullet bullet:bullets) {
            bullet.move();
        }
    }

    public void updateMine() {
        mine.update();
    }

    public void update() {
        if(MaxBullet > 1 && reload_time > 0) {
            --reload_time;
        }

        if(iceTmr > 0) {
            --iceTmr;
        }

        if(shield && shieldTmr > 0){
            --shieldTmr;
            if(shieldTmr == 0) {
                shield = false;
            }
        }

        if(freeze && freezeTmr > 0){
            --freezeTmr;
            if(freezeTmr == 0) {
                freeze = false;
            }
        }



        // todo -- commented updateBullets
        for(int i = 0; i < bullets.size(); i++) {
            if(bullets.get(i).recycle) {
                bullets.set(i,null);
            }
        }

        // Remove all recycled bullets
        while(bullets.remove(null));

        fire();
        if(!freeze) {
            move();
        }
        if(iceTmr == 1) {
            stopSlip();
        }

        // todo -- commented updateBullets
        updateBullets();


        updateMine();
    }

    public void setModel(MTank model, float scale) {
        this.x = (int) (model.x * scale);
        this.y = (int) (model.y * scale);
        this.direction = model.dirction;
        this.setShield(model.shield);
        this.setBoat(model.boat);
        this.armour = model.armour;
        this.freeze = model.freeze;

        if(model.mineDropped && !getMine().isDropped() && getMine().id != model.mineID) {
            super.dropMine();
        }
        if(model.mineExplode) {
            getMine().setExplosion();
        }

        if(model.lives != this.lives){
            this.lives = model.lives;

            updateLifeView();

//            ((TankActivity)TankView.context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (Player.this.type == ObjectType.ST_PLAYER_1) {
//                        ((TankActivity) (TankView.getInstance().getTankViewContext())).P1StatusTxt.setText(String.valueOf(Player.this.lives));
//                    } else {
//                        ((TankActivity) (TankView.getInstance().getTankViewContext())).P2StatusTxt.setText(String.valueOf(Player.this.lives));
//                    }
//
//                }
//            });
        }
        this.respawn = model.respawn;
        if(!destroyed && model.tDestroyed) {
            setDestroyed();
            svrKill = model.svrKill;
        }

        for(int i = 0; i < bullets.size(); i++) {
//            if(!bullets.get(i).isDestroyed() || bullets.get(i).recycle) {
//                bullets.set(i,null);
//            }
            if (bullets.get(i).recycle) {
                bullets.set(i, null);   // remove bullets ready for recycling
            }
        }

        while(bullets.remove(null));

        for (int i = 0; i < model.bullets.size(); i++) {
            boolean found = false;
            for(int j = 0; j < bullets.size(); j++) {
                if (model.bullets.get(i)[4] == bullets.get(j).id) {
                    found = true;
                    if (model.bullets.get(i)[3] == 1 && !bullets.get(j).isDestroyed()) {     // bullet should be destroyed now
                        bullets.get(j).x = (int) (model.bullets.get(i)[0] * scale);
                        bullets.get(j).y = (int) (model.bullets.get(i)[1] * scale);
                        bullets.get(j).setDestroyed();
                        bullets.get(j).svrKill = model.bullets.get(i)[5] == 1;  // which player caused the bullet destruction
                    }
                    break;
                }
            }
            if(!found) {
                if (model.bullets.get(i)[3] == 1 && model.bullets.get(i)[7] == 1) {
                    bullet.setDestroyed();
                    bullet.svrKill = model.bullets.get(i)[5] == 1;
                }
                else{
                    bullet.destroyed = false;
                }


                bullet.id = model.bullets.get(i)[4];
                bullet.move(model.bullets.get(i)[2]);
                bullet.setPlayer(model.player);

                bullets.add((new Bullet(bullet, (int) (model.bullets.get(i)[0] * scale), (int) (model.bullets.get(i)[1] * scale))));
                bullet.destroyed = false;
            }
        }
    }

    public void draw(Canvas canvas) {
        if(respawn) {
            if(frame >= spsprite.frame_count) {
                respawn = false;
                frame = 0;
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
            frame %= sprite.frame_count;

            if(freeze) {
                freezeOnTmr = (freezeOnTmr + 1) % freezBlinkTime;
                if(freezeOnTmr == 0) {
                    freezeOn = !freezeOn;
                }
            }


            if(!freeze || freezeOn) {
                canvas.drawBitmap(TankView.tankBitmap.get(sprite.frame_count * armour + frame).get(4 * group + direction), x, y, null);
            }

            if(frame_delay <= 0) {
                frame = (frame + 1) % sprite.frame_count;
                frame_delay = sprite.frame_time;
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
        }
        else {
            if (frame < dsprite.frame_count) {
                canvas.drawBitmap(dbitmap[frame], x - (int) (w / 2), y - (int) (h / 2), null);
                if(frame_delay <= 0) {
                    frame = frame + 1;
                    frame_delay = dsprite.frame_time;
                }
                else{
                    --frame_delay;
                }
            } else if (frame >= dsprite.frame_count) {
                if(lives > 0) {
                    respawn();
                }
                else{
                    this.x = 1000;
                    this.y = 1000;
                }
            }
        }

        for(Bullet bullet:bullets) {
            if(bullet != null) {
                bullet.draw(canvas);
            }
        }

        if(mine.isDropped() || mine.isExploding()){
            mine.draw(canvas);
        }
    }
}
