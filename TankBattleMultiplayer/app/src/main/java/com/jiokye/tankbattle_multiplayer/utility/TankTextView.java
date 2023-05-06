package com.jiokye.tankbattle_multiplayer.utility;




import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TankTextView extends androidx.appcompat.widget.AppCompatTextView {

    public TankTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TankTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TankTextView(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(this.getContext().getAssets(),"prstartk.ttf");
        setTypeface(tf);
    }
}
