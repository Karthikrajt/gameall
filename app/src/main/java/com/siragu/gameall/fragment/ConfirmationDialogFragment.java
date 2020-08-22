package com.siragu.gameall.fragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.siragu.gameall.R;

/**
 * Created by a_man on 27-12-2017.
 */

public class ConfirmationDialogFragment extends DialogFragment {
    private String title, message, noText, yesText;
    private View.OnClickListener yesClickListener, noClickListener;

    public ConfirmationDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_confirmation, null);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.message)).setText(message);
        TextView yes = ((TextView) view.findViewById(R.id.yes));
        TextView no = ((TextView) view.findViewById(R.id.no));
        yes.setText(yesText);
        no.setText(noText);
        view.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                yesClickListener.onClick(view);
            }
        });
        view.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                noClickListener.onClick(view);
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static ConfirmationDialogFragment newInstance(String title, String message, String yesText, String noText, View.OnClickListener yesClickListener, View.OnClickListener noClickListener) {
        ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
        dialogFragment.title = title;
        dialogFragment.message = message;
        dialogFragment.yesText = yesText;
        dialogFragment.noText = noText;
        dialogFragment.yesClickListener = yesClickListener;
        dialogFragment.noClickListener = noClickListener;
        return dialogFragment;
    }
}

