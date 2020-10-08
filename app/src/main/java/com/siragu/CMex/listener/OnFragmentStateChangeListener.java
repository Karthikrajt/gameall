package com.siragu.CMex.listener;

/**
 * Denotes various states of a fragment
 */
public interface OnFragmentStateChangeListener {
    void onDetachPostTypeFragment();
    void onPausePostTypeFragment();
    void onOtherPostTypeFragment(String i);
}
