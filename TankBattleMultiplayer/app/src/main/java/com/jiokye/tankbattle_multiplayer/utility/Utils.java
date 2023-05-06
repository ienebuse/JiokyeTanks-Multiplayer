package com.jiokye.tankbattle_multiplayer.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import androidx.fragment.app.FragmentActivity;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentTransaction;


import com.jiokye.tankbattle_multiplayer.fragments.StoreFragment;

public class Utils {

    public static class Effects {
        static ViewGroup.LayoutParams params;
        public static Animation blink(View v, int t) {
            Animation anim = new AlphaAnimation(0.5f, 1.0f);
            anim.setDuration(50);
//        anim.setStartOffset(200);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(t);
            v.startAnimation(anim);
            return anim;
        }

        public static ObjectAnimator zoom(View v, float scale, int repeat) {

            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    v,
                    PropertyValuesHolder.ofFloat("scaleX", scale),
                    PropertyValuesHolder.ofFloat("scaleY", scale)
            );
            objectAnimator.setDuration(500);
            objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
            objectAnimator.setRepeatCount(repeat == 0 ? ValueAnimator.INFINITE:repeat);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
//                    animation.removeListener(this);
                    animation.setDuration(0);
                    ((ValueAnimator)animation).reverse();
                }
            });
            objectAnimator.start();
            return  objectAnimator;
        }

        public static void end_zoom(ObjectAnimator objectAnimator) {
            if(objectAnimator != null) {
                objectAnimator.setDuration(0);
                objectAnimator.reverse();
                objectAnimator.end();
            }
        }

        public static void pause_rotation(ObjectAnimator objectAnimator) {
            if(objectAnimator != null && !objectAnimator.isPaused()) {
                objectAnimator.pause();
            }
        }

        public static void resume_rotation(ObjectAnimator objectAnimator) {
            if(objectAnimator != null && objectAnimator.isPaused()) {
                objectAnimator.resume();
            }
        }

        public static ObjectAnimator rotate(View v) {

            params = v.getLayoutParams();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "rotation", 0f, 3600f);
            objectAnimator.setDuration(20000);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.start();
            return  objectAnimator;
        }
    }

    public static class Store {
        public static StoreFragment openStore(Activity activity, int fragmentId) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            StoreFragment sf = new StoreFragment(activity);
            fragmentTransaction.replace(fragmentId, sf);
            fragmentTransaction.addToBackStack("cFragment");
            fragmentTransaction.commit();
            return sf;
        }

        public static void closeStore(FragmentActivity activity) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        }
    }
}
