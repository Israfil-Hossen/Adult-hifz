package com.israfilhossen.hifz;

import android.view.WindowManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Keeps the screen awake while the mushaf is open.
 *
 * The page asked for this with navigator.wakeLock, which is the right API and
 * is not implemented by Android's System WebView - so the request failed
 * silently and the screen dimmed mid-ayah, exactly as it always had. A window
 * flag is the thing that actually works, and only the Activity can set it.
 */
@CapacitorPlugin(name = "Screen")
public class ScreenPlugin extends Plugin {

    @PluginMethod
    public void keepAwake(PluginCall call) {
        final boolean on = Boolean.TRUE.equals(call.getBoolean("on", Boolean.TRUE));
        final android.app.Activity a = getActivity();
        if (a == null) { call.resolve(new JSObject().put("awake", false)); return; }
        a.runOnUiThread(new Runnable() {
            @Override public void run() {
                if (on) a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else    a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        call.resolve(new JSObject().put("awake", on));
    }
}
