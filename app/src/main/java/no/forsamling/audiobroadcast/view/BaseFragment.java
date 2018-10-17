package no.royalone.audiobroadcast.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import no.royalone.audiobroadcast.BaseApplication;


public class BaseFragment extends Fragment {

    protected Activity mMainActivity;
    private ProgressDialog mProgressDialog;
    protected LayoutInflater mLayoutInflater;

    public void showProgressDialog() {

        if (mProgressDialog != null) {
            return;
        }

        mProgressDialog = new ProgressDialog(mMainActivity);
        mProgressDialog.setMessage("Loading please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void dismissProgressDialog() {

        if (mProgressDialog != null) {
            try {
                mProgressDialog.dismiss();
            } catch (IllegalArgumentException e) {
            }
            mProgressDialog = null;
        }
    }

    @SuppressWarnings("unused")
    public boolean backAllowed() {
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mMainActivity =  activity;
        mLayoutInflater = LayoutInflater.from(mMainActivity);
    }

    @SuppressWarnings("unused")
    public void hideKeyboard() {
        View view = mMainActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("unused")
    public void showInfoDialog(String title, String description) {
        showInfoDialog(title, description, null);
    }

    public void showInfoDialog(String title, String description, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mMainActivity);
        builderSingle.setTitle(title);
        builderSingle.setMessage(description);

        if (okListener == null) {
            okListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            };
        } else {
            builderSingle.setCancelable(false);
        }

        builderSingle.setPositiveButton("OK", okListener);
        builderSingle.show();
    }

    @SuppressWarnings("unused")
    protected boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) BaseApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
