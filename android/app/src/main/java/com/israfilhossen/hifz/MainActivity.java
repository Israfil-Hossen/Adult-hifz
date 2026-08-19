package com.israfilhossen.hifz;

import android.os.Bundle;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    // Must run before super.onCreate so the splash theme is installed on
    // pre-31 devices too; on 31+ the system draws it from the theme anyway.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
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
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
