package com.siragu.CMex.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siragu.CMex.R;

/**
 * Created by a_man on 26-12-2017.
 */

public class BookmarksFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        inflatePosts();
        return view;
    }

    private void inflatePosts() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.frameBookmark, HomeFeedsFragment.newInstance("bookmark", "-1", false), "bookmark")
                .commit();
    }
}
