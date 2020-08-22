package com.siragu.gameall.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.onesignal.OneSignal;
import com.siragu.gameall.R;
import com.siragu.gameall.activity.SettingsActivity;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.request.UserUpdateRequest;
import com.siragu.gameall.network.response.UserResponse;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Provides the options for editing the Profile settings like username and gender
 */
public class SettingsProfileFragment extends Fragment {
    SwitchCompat[] switches = new SwitchCompat[2];

    private SharedPreferenceUtil sharedPreferenceUtil;
    private UserResponse profileResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_push_notification, container, false);
        switches[0] = view.findViewById(R.id.frag_settings_push_noti_like_switch_view);
        switches[1] = view.findViewById(R.id.frag_settings_push_noti_comment_switch_view);
        setHasOptionsMenu(true);

        sharedPreferenceUtil = new SharedPreferenceUtil(getContext());

        profileResponse = Helper.getLoggedInUser(sharedPreferenceUtil);
        switches[0].setChecked(profileResponse != null && profileResponse.getNotification_on_like());
        switches[1].setChecked(profileResponse != null && profileResponse.getNotification_on_comment());

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_settings_menu_done).setVisible(true);
        ((SettingsActivity) getActivity()).setActionBarSubtitle(getString(R.string.title_push_notification));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_settings_menu_done) {
            Helper.closeKeyboard(getActivity());
            updateNotificationSettingsCall();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes network call here if required and updates the settings in the {@link android.content.SharedPreferences}
     */
    private void updateNotificationSettingsCall() {
        DrService service = ApiUtils.getClient().create(DrService.class);
        service.createUpdateUser(sharedPreferenceUtil.getStringPreference(Constants.KEY_API_KEY, null),
                new UserUpdateRequest(profileResponse.getGender(),
                        OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId(),
                        switches[0].isChecked(), true, switches[1].isChecked(), profileResponse.getIs_private()), 1).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.settings_updated, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                t.getMessage();
            }
        });
    }
}
