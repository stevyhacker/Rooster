package com.blikoon.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A login screen that offers login via jid/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private AutoCompleteTextView jidTxtView;
    private EditText passwordEditTxt;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        jidTxtView = (AutoCompleteTextView) findViewById(R.id.email);
        passwordEditTxt = (EditText) findViewById(R.id.password);

        if (BuildConfig.DEBUG) {
            jidTxtView.setText("stevyhacker@conversations.im");
            passwordEditTxt.setText("stevan33");
        }

        passwordEditTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mJidSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (RoosterConnectionService.UI_AUTHENTICATED.equals(action)) {
                    Log.d(TAG, "Got a broadcast to show the main app window");
                    Intent i2 = new Intent(LoginActivity.this, ContactListActivity.class);
                    startActivity(i2);
                    finish();
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.UI_AUTHENTICATED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        jidTxtView.setError(null);
        passwordEditTxt.setError(null);

        // Store values at the time of the login attempt.
        String email = jidTxtView.getText().toString();
        String password = passwordEditTxt.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditTxt.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditTxt;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            jidTxtView.setError(getString(R.string.error_field_required));
            focusView = jidTxtView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            jidTxtView.setError(getString(R.string.error_invalid_jid));
            focusView = jidTxtView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            //Save the credentials and login
            saveCredentialsAndLogin();

        }
    }

    private void saveCredentialsAndLogin() {
        Log.d(TAG, "saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", jidTxtView.getText().toString())
                .putString("xmpp_password", passwordEditTxt.getText().toString())
                .putBoolean("xmpp_logged_in", true)
                .apply();

        //Start the service
        Intent i1 = new Intent(this, RoosterConnectionService.class);
        startService(i1);

    }

    private boolean isEmailValid(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

}

