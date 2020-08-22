package com.siragu.gameall.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by Arvind on 3/27/2018.
 */

public class MontserratTextView extends AppCompatTextView {
    public MontserratTextView(Context context) {
        super(context);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "montserrat_regular.ttf"));
    }

    public MontserratTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "montserrat_regular.ttf"));
    }

    public MontserratTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "montserrat_regular.ttf"));
    }

}
