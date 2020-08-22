package com.siragu.gameall.util;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * A util class for PIN input
 */
public class MyPinInputHelper implements TextWatcher, View.OnFocusChangeListener {

    private Activity activity;
    private int currentViewNum;
    private EditText otp[];
    private EditText currentView;

    /**
     * Initializes the {@link MyPinInputHelper}
     * @param activity
     * @param editTexts Multiple {@link EditText} to combine them into one input
     */
    public static void build(Activity activity, EditText[] editTexts){
        MyPinInputHelper myPinInputHelper = new MyPinInputHelper();
        myPinInputHelper.otp = editTexts;
        myPinInputHelper.activity = activity;
        myPinInputHelper.prepareOtpInput();
    }

    /**
     * Prepares {@link MyPinInputHelper} to start taking input and focussing on appropriate views
     */
    private void prepareOtpInput(){

        for (EditText otpNum : otp) {
            otpNum.addTextChangedListener(this);
            otpNum.setOnFocusChangeListener(this);
        }

        currentView = otp[0];
        currentViewNum = 0;
        otp[0].requestFocus();
        //Helper.openKeyboard(activity);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        int prev = currentViewNum - 1;
        int current = currentViewNum;
        int next = currentViewNum + 1;

        if (charSequence.length() == 0){
            if (current > 0){
                otp[current].setFocusableInTouchMode(false);
                otp[prev].setFocusableInTouchMode(true);
                otp[prev].requestFocus();
                otp[prev].setSelection(otp[prev].getText().length());
            }
        } else if (charSequence.length() == 2) {
            if (current < (otp.length-1)){
                if (TextUtils.isEmpty(otp[next].getText())) {
                    otp[next].setFocusableInTouchMode(true);
                    otp[current].setFocusableInTouchMode(false);
                    otp[current].setText(String.valueOf(charSequence.charAt(0)));
                    otp[next].setText(String.valueOf(charSequence.charAt(1)));
                    otp[next].requestFocus();
                    otp[next].setSelection(otp[next].getText().length());
                } else {
                    otp[current].setText(String.valueOf(otp[current].getText().charAt(0)));
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        currentView = (EditText) view;
        for (int i = 0; i < otp.length; i++) {
            if (currentView == otp[i]){
                currentViewNum = i;
                break;
            }
        }
    }
}
