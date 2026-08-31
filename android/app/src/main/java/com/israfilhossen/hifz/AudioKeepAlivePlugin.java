package com.israfilhossen.hifz;

import android.content.Intent;
import android.os.Build;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * The web layer's bridge to {@link PlaybackService}.
 *
 * handoff(queue, speed): the page is going dark mid-recitation. It sends the
 * REMAINING ayat as a list of URL-lists (every host per ayah, in order) and the
 * service plays them natively - the only audio Android reliably keeps alive
 * with the screen off.
 *
 * status(): the page is back. Returns how far the native player got, so the
 * web side can resume from that ayah, then call stop().
 */
@CapacitorPlugin(name = "AudioKeepAlive")
public class AudioKeepAlivePlugin extends Plugin {

    @PluginMethod
    public void handoff(PluginCall call) {
        try {
            JSArray arr = call.getArray("queue");
            List<List<String>> q = new ArrayList<>();
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONArray srcs = arr.getJSONArray(i);
                    List<String> one = new ArrayList<>();
                    for (int j = 0; j < srcs.length(); j++) one.add(srcs.getString(j));
                    q.add(one);
                }
            }
            PlaybackService.QUEUE = q;
            PlaybackService.INDEX = 0;
            Double sp = call.getDouble("speed");
            PlaybackService.SPEED = sp == null ? 1.0f : sp.floatValue();

            Intent i = new Intent(getContext(), PlaybackService.class);
            i.putExtra("play", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(i);
            } else {
                getContext().startService(i);
            }
            call.resolve();
        } catch (Exception e) {
            /* never fail the caller - audio that stops in the background is
               still better than a page that errors in the foreground */
            call.resolve();
        }
    }

    @PluginMethod
    public void status(PluginCall call) {
        JSObject o = new JSObject();
        o.put("index", PlaybackService.INDEX);
        o.put("playing", PlaybackService.PLAYING);
        o.put("queued", PlaybackService.QUEUE.size());
        call.resolve(o);
    }

    @PluginMethod
    public void start(PluginCall call) {
        /* keep-alive only, no native playback - retained for compatibility */
        try {
            Intent i = new Intent(getContext(), PlaybackService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(i);
            } else {
                getContext().startService(i);
            }
        } catch (Exception ignored) {}
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            PlaybackService.QUEUE = new ArrayList<>();
            PlaybackService.PLAYING = false;
            getContext().stopService(new Intent(getContext(), PlaybackService.class));
        } catch (Exception ignored) {}
        call.resolve();
    }
}
