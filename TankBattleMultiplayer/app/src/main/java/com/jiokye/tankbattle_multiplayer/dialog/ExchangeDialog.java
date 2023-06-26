package com.jiokye.tankbattle_multiplayer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.jiokye.tankbattle_multiplayer.R;
import com.jiokye.tankbattle_multiplayer.utility.Utils;

public class ExchangeDialog extends Dialog {

    Context context;
    ImageView confirmBtn, closeBtn;
    ConstraintLayout dialogheader,dialogHeader;
    int item, cost;
    ConfirmExchange confirmExchange;

    public ExchangeDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public ExchangeDialog(@NonNull Context context, int item, int cost) {
        super(context);
        this.item = item;
        this.cost = cost;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.dialog_exchange);

        confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(dialogListener);
        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(dialogListener);
//        dialogheader = findViewById(R.id.dialogheader);

        dialogHeader = findViewById(R.id.dialogheader);
        ImageView exchangeItem1 = findViewById(R.id.exchangeItem1);
        ImageView exchangeItem2 = findViewById(R.id.exchangeItem2);
        ImageView exchangeValue = findViewById(R.id.exchangeValue);
        TextView exchangeQty = findViewById(R.id.exchangeQty);
        TextView itemQty = findViewById(R.id.itemQty);
        RelativeLayout exchangeTextField = findViewById(R.id.exchangeTextField);
        ImageView confirmBtn = findViewById(R.id.confirmBtn);

        switch (item) {
            case 1:
                dialogHeader.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.freegoldheader,null));
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeTextField.setVisibility(View.INVISIBLE);
                confirmBtn.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.videobtn,null));
                itemQty.setText("x2");
                break;
            case 2:
                exchangeItem1.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.itemstartboat,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.VISIBLE);
                exchangeItem2.setVisibility(View.INVISIBLE);
                itemQty.setText("x2");
                break;
            case 3:
                exchangeItem1.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.itemclockshovel,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.VISIBLE);
                exchangeItem2.setVisibility(View.INVISIBLE);
                itemQty.setText("x2");
                break;
            case 4:
                exchangeItem1.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.itemhelmetgun,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.VISIBLE);
                exchangeItem2.setVisibility(View.INVISIBLE);
                itemQty.setText("x2");
                break;
            case 5:
                exchangeItem1.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.itemgrenadetank,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.VISIBLE);
                exchangeItem2.setVisibility(View.INVISIBLE);
                itemQty.setText("x2");
                break;
            case 6:
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.bonus_mine,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                itemQty.setText("x2");
                break;
            case 7:
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.game,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/10");
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                itemQty.setText("x1");
                break;
            case 8:
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.game6h,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/30");
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                itemQty.setText("x1");
                break;
            case 9:
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.adcoin,null));
                exchangeQty.setText("tankstages/20");
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                itemQty.setText("x10");
                break;
            case 10:
                exchangeItem2.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.bonus_builder,null));
                exchangeValue.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.gold,null));
                exchangeQty.setText("tankstages/5");
                exchangeItem1.setVisibility(View.INVISIBLE);
                exchangeItem2.setVisibility(View.VISIBLE);
                itemQty.setText("x3");
                break;
        }


        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
    }

    View.OnClickListener dialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            Utils.Effects.blink(view,1).setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(id == R.id.confirmBtn) {
                        confirmExchange.confirmExchange(item,cost);
                    }
                    else {
                        confirmExchange.confirmExchange(0,cost);
                    }
                    dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

        }
    };


    public void onWindowFocusChanged (boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void setConfirmExchange(ConfirmExchange confirmExchange) {
        this.confirmExchange = confirmExchange;
    }


    public interface ConfirmExchange {
        public void confirmExchange(int item, int cost);
    }
}
