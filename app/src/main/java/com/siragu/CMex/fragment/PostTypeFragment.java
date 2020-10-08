package com.siragu.CMex.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siragu.CMex.listener.OnFragmentStateChangeListener;
import com.siragu.CMex.R;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.util.Helper;
import com.siragu.CMex.util.SharedPreferenceUtil;
import com.siragu.CMex.util.SpringAnimationHelper;

/**
 * A {@link Fragment} with options to post icon_text, icon_picture or video
 */
public class PostTypeFragment extends Fragment implements View.OnClickListener {

    private OnFragmentStateChangeListener onFragmentStateChangeListener;
    private UserResponse userMe;
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentStateChangeListener) {
            onFragmentStateChangeListener = (OnFragmentStateChangeListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnFragmentStateChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentStateChangeListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_add_text_pic, container, false);
        view.findViewById(R.id.frag_add_text_pic_txt).setOnClickListener(this);
        view.findViewById(R.id.frag_add_text_pic_pic).setOnClickListener(this);
        view.findViewById(R.id.frag_add_text_pic_video).setOnClickListener(this);

        sharedPreferenceUtil = new SharedPreferenceUtil(getContext());
        userMe = Helper.getLoggedInUser(sharedPreferenceUtil);
        if (userMe != null)
        {
            if(userMe.getId().toString().toLowerCase().equals("10"))
            {
                view.findViewById(R.id.frag_add_text_pic_video).setVisibility(View.VISIBLE);
            }
            else
            {
                view.findViewById(R.id.frag_add_text_pic_video).setVisibility(View.GONE);
            }
        }
        else
        {
            view.findViewById(R.id.frag_add_text_pic_video).setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        onFragmentStateChangeListener.onPausePostTypeFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frag_add_text_pic_empty_view:
                try {
                    if (getActivity() != null)
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                } catch (Exception ex) {
                    Log.e("closeThis", ex.toString());
                }
                break;
            case R.id.frag_add_text_pic_video:
                SpringAnimationHelper.performAnimation(v);
                if (onFragmentStateChangeListener != null)
                    onFragmentStateChangeListener.onOtherPostTypeFragment("video");
                break;
            case R.id.frag_add_text_pic_pic:
                SpringAnimationHelper.performAnimation(v);
                if (onFragmentStateChangeListener != null)
                    onFragmentStateChangeListener.onOtherPostTypeFragment("image");
                break;
            case R.id.frag_add_text_pic_txt:
                SpringAnimationHelper.performAnimation(v);
                if (onFragmentStateChangeListener != null)
                    onFragmentStateChangeListener.onOtherPostTypeFragment("text");
                break;
        }
    }
}
