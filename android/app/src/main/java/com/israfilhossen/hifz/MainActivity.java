package com.israfilhossen.hifz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.BridgeActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BridgeActivity {

    // Must run before super.onCreate so the splash theme is installed on
    // pre-31 devices too; on 31+ the system draws it from the theme anyway.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        // must be registered before the bridge starts, or the web layer calls
        // a plugin that does not exist yet
        registerPlugin(AudioKeepAlivePlugin.class);
        registerPlugin(ReminderPlugin.class);
        super.onCreate(savedInstanceState);

        /* Android 15 forces edge-to-edge on anything targeting SDK 35+, and at
           SDK 36 there is no opt-out. The page therefore starts at y=0, under
           the clock and the battery.

           The stylesheet already asks for the room — calc(6px +
           env(safe-area-inset-top)) and four more like it — but an Android
           WebView never reports the status bar as a safe-area inset, so that
           env() is 0 and the padding silently amounts to nothing.

           So the room is given here instead, to the view that holds the web
           view. Nothing in the web app has to know. The strip this exposes
           behind the bars is painted by android:windowBackground, which is set
           to the app's own paper colour in values/ and values-night/. */
        /* Ask for the microphone and notifications HERE, at launch, from the
           activity itself. They used to be requested mid-recording, from inside
           the WebView's getUserMedia flow - and granting one there tore down
           the WebView on some phones (MIUI among them), which the user saw as
           the app closing the moment they said yes. Granted up front, that
           in-flight request never happens; recording just starts.

           No storage permission is requested because none is needed: downloads
           live in the app's own cache, which has been permission-free since
           Android 4.4. */
        List<String> ask = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ask.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ask.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!ask.isEmpty()) {
            ActivityCompat.requestPermissions(this, ask.toArray(new String[0]), 7301);
        }

        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
