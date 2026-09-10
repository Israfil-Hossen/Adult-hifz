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

    /* the one live instance, so the service can pass a button press back */
    private static AudioKeepAlivePlugin SELF;

    @Override
    public void load() { SELF = this; }

    /** A transport button was pressed while the page owns the audio. */
    static void tell(String what) {
        AudioKeepAlivePlugin me = SELF;
        if (me == null) return;
        JSObject o = new JSObject();
        o.put("action", what);
        me.notifyListeners("transport", o);
    }

    /** note({labels, reciter, index, playing}): show the media notification for
     *  a recitation the PAGE is playing. No audio changes hands. */
    @PluginMethod
    public void note(PluginCall call) {
        try {
            PlaybackService.LABELS = readStrings(call.getArray("labels"));
            PlaybackService.RECITER = call.getString("reciter", "");
            Integer at = call.getInt("index");
            PlaybackService.INDEX = at == null ? 0 : at;
            Boolean pl = call.getBoolean("playing", Boolean.TRUE);
            PlaybackService.PAGE_PLAYING = pl == null || pl;
            Double dur = call.getDouble("durMs");
            PlaybackService.DUR_MS = dur == null ? 0L : dur.longValue();
            Double at2 = call.getDouble("atMs");
            PlaybackService.AT_MS = at2 == null ? 0L : at2.longValue();
            PlaybackService.DONE = false;

            Intent i = new Intent(getContext(), PlaybackService.class);
            i.putExtra("note", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(i);
            } else {
                getContext().startService(i);
            }
        } catch (Exception ignored) {}
        call.resolve();
    }

    /** the page stopped: take the notification down */
    @PluginMethod
    public void noteStop(PluginCall call) {
        try {
            PlaybackService.NOTE_ONLY = false;
            getContext().stopService(new Intent(getContext(), PlaybackService.class));
        } catch (Exception ignored) {}
        call.resolve();
    }

    @PluginMethod
    public void handoff(PluginCall call) {
        try {
            List<List<String>> q = readQueue(call.getArray("queue"));
            PlaybackService.QUEUE = q;
            PlaybackService.INDEX = 0;
            PlaybackService.LOOP = readQueue(call.getArray("loop"));
            /* what the notification and the lock screen will say: one line per
               ayah, written by the web layer because only it knows the surah
               names in the reader's language */
            PlaybackService.LABELS = readStrings(call.getArray("labels"));
            PlaybackService.RECITER = call.getString("reciter", "");
            Integer pos = call.getInt("posMs");
            PlaybackService.POS_MS = pos == null ? 0 : pos;
            Integer rep = call.getInt("repeat");
            PlaybackService.REPEAT = rep == null ? 0 : rep;
            PlaybackService.PASSES = 0;
            PlaybackService.DONE = false;
            Double at = call.getDouble("stopAt");
            PlaybackService.STOP_AT = at == null ? 0L : at.longValue();
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

    private static java.util.List<String> readStrings(JSArray arr) throws Exception {
        java.util.List<String> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) out.add(String.valueOf(arr.get(i)));
        return out;
    }

    /* a list of per-ayah source lists, in the order the web layer would try them */
    private static List<List<String>> readQueue(JSArray arr) throws Exception {
        List<List<String>> q = new ArrayList<>();
        if (arr == null) return q;
        for (int i = 0; i < arr.length(); i++) {
            JSONArray srcs = arr.getJSONArray(i);
            List<String> one = new ArrayList<>();
            for (int j = 0; j < srcs.length(); j++) one.add(srcs.getString(j));
            q.add(one);
        }
        return q;
    }

    @PluginMethod
    public void status(PluginCall call) {
        JSObject o = new JSObject();
        o.put("index", PlaybackService.INDEX);
        o.put("playing", PlaybackService.PLAYING);
        o.put("queued", PlaybackService.QUEUE.size());
        o.put("passes", PlaybackService.PASSES);
        o.put("repeat", PlaybackService.REPEAT);
        o.put("done", PlaybackService.DONE);
        o.put("posMs", PlaybackService.POS_MS);
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
            PlaybackService.LOOP = new ArrayList<>();
            PlaybackService.LABELS = new ArrayList<>();
            PlaybackService.REPEAT = 0;
            PlaybackService.POS_MS = 0;
            PlaybackService.PLAYING = false;
            getContext().stopService(new Intent(getContext(), PlaybackService.class));
        } catch (Exception ignored) {}
        call.resolve();
    }
}
