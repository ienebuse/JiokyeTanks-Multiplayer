package com.jiokye.tankbattle_multiplayer.utility;

import android.app.Activity;
import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Debug;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppManager {
//    private static final String GOOGLE_PLAY_STORE = "com.android.vending";
//    private static final String AMAZON_APP_STORE = "com.amazon.venezia";
    private static final List<String> validInstallers = new ArrayList<>(Arrays.asList(
            AppManager.getAppString(new int[]{614, 1570, 664, 560, 604, 1560, 620, 1620, 674, 1510, 620, 560, 730, 1450, 670, 1440, 644, 1560, 634}),
            AppManager.getAppString(new int[]{614, 1570, 664, 560, 634, 1570, 674, 1470, 660, 1450, 270, 1410, 670, 1440, 710, 1570, 644, 1440, 270, 1460, 624, 1450, 620, 1420, 604, 1430, 654}),
            AppManager.getAppString(new int[]{614, 1570, 664, 560, 604, 1550, 604, 1720, 674, 1560, 270, 1660, 624, 1560, 624, 1720, 644, 1410})));

    private static final int[] ob = {644, 1250, 510, 1570, 450, 1510, 530, 1250, 500, 1700, 334, 1060, 340, 1460, 604, 1140, 660, 1010, 510, 1250, 714, 1070, 274, 1620, 414, 1270, 424, 750};

    static ArrayList<OnAppManagerSignal> onAppManagerSignals = new ArrayList<>();

    public static void checkDebugger() {
        if(Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            sendNotifications();
        }
    }

    public static void verifyInstaller (Context context) {
        if(!checkInstaller(context)) {
            sendNotifications();
        }
    }

    public static void verifySignature(Context context, String expectedSignature) {
        Signature signature = getAppSignature(context);
        if (signature != null) {
            String currentSignature = string(signature);
            if (currentSignature == null || !currentSignature.equals(expectedSignature)) {
                sendNotifications();
            }

        }
    }

    public static String getSignature(Context context) {
        Signature signature = getAppSignature(context);
        if (signature != null) {
            return string(signature);
        }
        return null;
    }

    private static boolean checkInstaller(Context context, String id) {
         try{

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo installSourceInfo = context.getPackageManager().getInstallSourceInfo(context.getPackageName());
                String installingPackageName = installSourceInfo.getInstallingPackageName();
//                return installingPackageName != null && installingPackageName.startsWith(id);
                 return installingPackageName != null && installingPackageName.startsWith(id);

            }
            else {
                String installingPackageName = context.getPackageManager().getInstallerPackageName(context.getPackageName());
                return installingPackageName != null && installingPackageName.startsWith(id);
            }
        }catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
        return false;
    }

    private static boolean checkInstaller(Context context) {
        try{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo installSourceInfo = context.getPackageManager().getInstallSourceInfo(context.getPackageName());
                String installingPackageName = installSourceInfo.getInstallingPackageName();
                return installingPackageName != null && validInstallers.contains(installingPackageName);

            }
            else {
                String installingPackageName = context.getPackageManager().getInstallerPackageName(context.getPackageName());
                return installingPackageName != null && validInstallers.contains(installingPackageName);
            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Signature getAppSignature(Context context) {
        Signature[] signatures;
        Signature signature = null;
        try {
            if (Build.VERSION.SDK_INT < 28) {
                signatures = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
                if(signatures != null && signatures.length > 0){
                    signature = signatures[0];
                }
            }
            else {
                SigningInfo signingInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;
                signatures = signingInfo.getApkContentsSigners();
                if (signatures != null && signatures.length > 0) {
                    signature = signatures[0];
                }
            }
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return signature;
    }


    private static String string(Signature signature) {
        String str;
        try {
            byte[] signatureBytes = signature.toByteArray();
            MessageDigest digest = MessageDigest.getInstance("SHA");
            byte[] hash = digest.digest(signatureBytes);
            str = Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception var4) {
            str = null;
        }

        return str;
    }

    public interface OnAppManagerSignal {
        void executeSignal();
    }

    public static void setAppManagerListener(OnAppManagerSignal onAppManagerSignal) {
        onAppManagerSignals.add(onAppManagerSignal);
    }

    private static void sendNotifications() {
//        new CountDownTimer((long)(Math.random()*30000),1000) {
//
//            @Override
//            public void onTick(long l) {}
//
//            @Override
//            public void onFinish() {
//                for(OnAppManagerSignal onAppManagerSignal: onAppManagerSignals) {
//                    onAppManagerSignal.executeSignal();
//                };
//            }
//        }.start();
    }

    public static String getAppString() {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < ob.length; i++) {
            int octVal = Integer.parseInt(String.valueOf(ob[i]),8);
            if(i%2 == 0) {
                octVal >>= 2;
            }
            else {
                octVal >>= 3;
            }
            str.append((char)octVal);
        }
        return str.toString();
    }

    public static String getAppString(int[] encob) {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < encob.length; i++) {
            int octVal = Integer.parseInt(String.valueOf(encob[i]),8);
            if(i%2 == 0) {
                octVal >>= 2;
            }
            else {
                octVal >>= 3;
            }
            str.append((char)octVal);
        }
        return str.toString();
    }

}
