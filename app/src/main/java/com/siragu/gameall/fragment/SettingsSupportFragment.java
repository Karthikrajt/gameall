package com.siragu.gameall.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.siragu.gameall.BuildConfig;
import com.siragu.gameall.R;

/**
 * Displays the contact information like email address
 */
public class SettingsSupportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings_support, container, false);
        view.findViewById(R.id.frag_settings_support_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getActivity().getResources().getString(R.string.support_email), null));
                i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                String body = "[ " + Build.MANUFACTURER + ", " + Build.BRAND + ", " + Build.MODEL + ", " + Build.PRODUCT + ", " + Build.DEVICE
                        + ", " + Build.VERSION.SDK_INT + ", " + Build.VERSION.RELEASE + ", " + BuildConfig.VERSION_NAME + "]\n\n";
                i.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(i, "Send email"));
            }
        });
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_settings_menu_done).setVisible(true);
        //((SettingsActivity) getActivity()).setActionBarSubtitle("Support");
        super.onPrepareOptionsMenu(menu);
    }
}
