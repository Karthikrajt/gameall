package com.siragu.gameall.listener;

/**
 * Denotes various states of a fragment
 */
public interface OnFragmentStateChangeListener {
    void onDetachPostTypeFragment();
    void onPausePostTypeFragment();
    void onOtherPostTypeFragment(String i);
}
