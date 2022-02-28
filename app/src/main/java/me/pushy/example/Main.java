package me.pushy.example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import me.pushy.sdk.Pushy;

public class Main extends AppCompatActivity {
    TextView mInstructions;
    TextView mDeviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load main.xml layout
        setContentView(R.layout.main);

        // Cache TextView objects
        mDeviceToken = findViewById(R.id.deviceToken);
        mInstructions = findViewById(R.id.instructions);

        // To send notifications to this app using your own Pushy account,
        // uncomment the below line of code and replace PUSHY_APP_ID with a Pushy App ID in your account
        // (Pushy Dashboard -> Click your app -> App Settings -> App ID)
        //
        // Pushy.setAppId("PUSHY_APP_ID", this);

        // Not registered yet?
        if (getDeviceToken() == null) {
            // Register with Pushy
            new RegisterForPushNotificationsAsync().execute();
        }
        else {
            // Start Pushy notification service if not already running
            Pushy.listen(this);

            // Update UI with device token
            updateUI();
        }

        // Enable FCM Fallback Delivery
        Pushy.toggleFCM(true, this);
    }

    private class RegisterForPushNotificationsAsync extends AsyncTask<String, Void, Exception> {
        ProgressDialog mLoading;

        public RegisterForPushNotificationsAsync() {
            // Create progress dialog and set it up
            mLoading = new ProgressDialog(Main.this);
            mLoading.setMessage(getString(R.string.registeringDevice));
            mLoading.setCancelable(false);

            // Show it
            mLoading.show();
        }

        @Override
        protected Exception doInBackground(String... params) {
            try {
                // Assign a unique token to this device
                String deviceToken = Pushy.register(Main.this);

                // Save token locally / remotely
                saveDeviceToken(deviceToken);
            }
            catch (Exception exc) {
                // Return exc to onPostExecute
                return exc;
            }

            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Activity died?
            if (isFinishing()) {
                return;
            }

            // Hide progress bar
            mLoading.dismiss();

            // Registration failed?
            if (exc != null) {
                // Write error to logcat
                Log.e("Pushy", "Registration failed: " + exc.getMessage());

                // Display error dialog
                new AlertDialog.Builder(Main.this).setTitle(R.string.registrationError)
                        .setMessage(exc.getMessage())
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
            }

            // Update UI with registration result
            updateUI();
        }
    }

    private void updateUI() {
        // Get device token from SharedPreferences
        String deviceToken = getDeviceToken();

        // Registration failed?
        if (deviceToken == null) {
            // Display registration failed in app UI
            mInstructions.setText(R.string.restartApp);
            mDeviceToken.setText(R.string.registrationFailed);

            // Stop execution
            return;
        }

        // Display device token
        mDeviceToken.setText(deviceToken);

        // Display "copy from logcat" instructions
        mInstructions.setText(R.string.copyLogcat);

        // Write device token to logcat
        Log.d("Pushy", "Device token: " + deviceToken);
    }

    private String getDeviceToken() {
        // Get token stored in SharedPreferences
        return getSharedPreferences().getString("deviceToken", null);
    }

    private void saveDeviceToken(String deviceToken) {
        // Save token locally in app SharedPreferences
        getSharedPreferences().edit().putString("deviceToken", deviceToken).commit();

        // Your app should store the device token in your backend database
        //new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
