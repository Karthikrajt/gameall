package com.siragu.gameall.fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.siragu.gameall.adapter.UniversalPagerAdapter;
import com.siragu.gameall.R;

public class HomeFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.activity_profile_view_pager);
        tabLayout = view.findViewById(R.id.frag_profile_tab_layout);
        tabLayout.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewPager();
    }

    private void initViewPager() {
        UniversalPagerAdapter adapter = new UniversalPagerAdapter(getChildFragmentManager());
            adapter.addFrag(HomeFeedsFragment.newInstance("hot", "-1", true), getString(R.string.title_feeds_public));
        //  adapter.addFrag(HomeFeedsFragment.newInstance("feed", "-1", false), getString(R.string.title_feeds_private));
        viewPager.setAdapter(adapter);
        
        tabLayout.setupWithViewPager(viewPager);
        reduceMarginsInTabs(tabLayout, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, getResources().getDisplayMetrics()));
    }

    public static void reduceMarginsInTabs(TabLayout tabLayout, int marginOffset) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }

            tabLayout.requestLayout();
        }
    }
}
