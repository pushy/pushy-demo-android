package me.pushy.example;

import android.app.Application;
import android.os.Build;

import me.pushy.sdk.Pushy;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Force Pushy foreground service on Google devices (Pixel / Emulator, Android 14+)
        // To work around Cached Apps Freezer which is enabled by default
        if (shouldForceForegroundService()) {
            Pushy.toggleForegroundService(true, this);
        }
    }

    public static boolean shouldForceForegroundService() {
        // Force Pushy foreground service on Google devices (Pixel / Emulator, Android 14+)
        return Build.MANUFACTURER.toLowerCase().contains("google") && Build.VERSION.SDK_INT >= 34;
    }
}
