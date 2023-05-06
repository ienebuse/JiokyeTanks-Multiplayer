package com.jiokye.tankbattle_multiplayer.utility;

public class CONST {
    public static class Direction {
        public static final int UP = 0;
        public static final int RIGHT = 1;
        public static final int DOWN = 2;
        public static final int LEFT = 3;
    }

    public static class Tank {
        public static final int FSP = 32;
        public static final int MAX_GAME_COUNT = 5;
        public static final int LIFE_DURATION_MINS = 60;
        public static final long LIFE_DURATION_6HRS = 3*60*60*1000;
        public static final int NUM_LEVELS = 35;
        public static final int NUM_OBJECTIVES = 11;
        public static final int NUM_ENEMIES = 20;
        public static final int MAX_ITEM = 10;
        public static final int MAX_GOLD = 1000;
        public static final int MAX_ADCOIN = 1000;
        public static final float GIFT_THR1 = 0.3f;
        public static final float GIFT_THR2 = 0.6f;
        public static final float GIFT_THR3 = 0.9f;


//        public static final int[] TankMenuActivity_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};
//        public static final int[] TankMenuActivity_RIAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 630, 324, 640, 300, 640, 330, 630, 334, 710};
//        public static final int[] TankMenuActivity_IAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 304, 600, 314, 630, 304, 670, 314, 670, 304, 620};
//        public static final int[] TankMenuActivity_BAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 330, 630, 300, 600, 344, 670, 340, 610, 304, 610};
//
//        public static final int[] TankActivity_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};
//        public static final int[] TankActivity_RIAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 630, 324, 640, 300, 640, 330, 630, 334, 710};
//        public static final int[] TankActivity_IAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 304, 600, 314, 630, 304, 670, 314, 670, 304, 620};
//
//        public static final int[] TankDailyReward_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};
//
//        public static final int[] TankEndGame_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};
//
//        public static final int[] TankStore_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};



        public static final int[] TankMenuActivity_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 344, 650, 310, 640, 324, 660, 330, 670, 330, 620};
        public static final int[] TankMenuActivity_RIAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 334, 650, 320, 620, 334, 630, 300, 620, 320, 620};
        public static final int[] TankMenuActivity_IAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 304, 620, 310, 640, 334, 630, 314, 710, 300, 620};
        public static final int[] TankMenuActivity_BAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 344, 670, 340, 650, 334, 650, 300, 620, 300, 640};

//        public static final int[] TankActivity_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 630, 344, 640, 300, 620, 324, 660, 300, 710, 344, 710, 320, 620, 324, 640, 320, 570, 324, 620, 310, 640, 314, 650, 320, 710, 304, 670};
        public static final int[] TankActivity_RIAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 340, 600, 340, 710, 324, 620, 324, 610, 324, 610};
        public static final int[] TankActivity_IAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 314, 630, 340, 700, 334, 630, 320, 710, 324, 620};

        public static final int[] TankDailyReward_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 304, 600, 310, 710, 310, 710, 310, 670, 340, 630};

        public static final int[] TankEndGame_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 310, 650, 330, 650, 334, 700, 344, 660, 314, 660};

        public static final int[] TankStore_RAD = {614, 1410, 264, 1410, 700, 1600, 264, 1600, 724, 1420, 264, 710, 310, 610, 300, 710, 324, 660, 314, 620, 324, 650, 320, 640, 304, 640, 344, 570, 330, 700, 340, 600, 314, 700, 324, 620, 330, 620};

    }

    public static final String GAME_DATA_KEY = "GAME_DATA";
    public static final String GAME_NAME = "TANK";
    public static final String PLAYER_INFO = "PLAYER_INFO";
    public static final String STRING_INFO = "STRING_INFO";
}
