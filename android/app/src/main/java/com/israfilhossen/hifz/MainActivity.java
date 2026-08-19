package com.israfilhossen.hifz;

import android.os.Bundle;
import androidx.core.splashscreen.SplashScreen;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    // Must run before super.onCreate so the splash theme is installed on
    // pre-31 devices too; on 31+ the system draws it from the theme anyway.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
    }
}
