package com.siragu.CMex.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class MontserratEditText extends AppCompatEditText {
    public MontserratEditText(Context context) {
        super(context);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "HelveticaNeue.ttf"));
    }

    public MontserratEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "HelveticaNeue.ttf"));
    }

    public MontserratEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "HelveticaNeue.ttf"));
    }
}
