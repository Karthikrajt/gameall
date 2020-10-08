package com.siragu.CMex.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class MontserratBoldTextView extends AppCompatTextView {
    public MontserratBoldTextView(Context context) {
        super(context);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Helvetica Neu Bold.ttf"));
    }

    public MontserratBoldTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Helvetica Neu Bold.ttf"));
    }

    public MontserratBoldTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Helvetica Neu Bold.ttf"));
    }

}
