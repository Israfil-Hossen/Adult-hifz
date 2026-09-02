package com.israfilhossen.hifz;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Where downloaded recitation actually lives.
 *
 * The web layer kept it in Cache Storage, which belongs to the WebView and to
 * nothing else. So a reader could download a whole juz, turn the network off,
 * hear it play - and lose it the moment the screen went dark, because the
 * handoff to {@link PlaybackService} can only give Android's MediaPlayer an
 * address it can open, and a blob: URL is not one.
 *
 * These files are. save() fetches on the native side (nothing crosses the
 * bridge but a URL and a path), and the paths it returns work in the WebView
 * too, through Capacitor.convertFileSrc.
 */
@CapacitorPlugin(name = "AudioStore")
public class AudioStorePlugin extends Plugin {

    private File dir() {
        File d = new File(getContext().getFilesDir(), "recite");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    /** "2:255" under reciter "ar.alafasy" becomes ar.alafasy__2_255.mp3 */
    private static String nameOf(String reciter, String key) {
        String r = (reciter == null ? "" : reciter).replaceAll("[^A-Za-z0-9._-]", "_");
        String k = (key == null ? "" : key).replaceAll("[^0-9]", "_");
        return r + "__" + k + ".mp3";
    }

    private File fileFor(String reciter, String key) {
        return new File(dir(), nameOf(reciter, key));
    }

    @PluginMethod
    public void save(PluginCall call) {
        String key = call.getString("key"), reciter = call.getString("reciter", "");
        JSArray urls = call.getArray("urls");
        if (key == null || urls == null) { call.reject("key and urls are required"); return; }

        File out = fileFor(reciter, key);
        if (out.exists() && out.length() > 0) {
            call.resolve(new JSObject().put("saved", true).put("path", out.getAbsolutePath()));
            return;
        }

        List<String> list = new ArrayList<>();
        try {
            JSONArray a = urls;
            for (int i = 0; i < a.length(); i++) list.add(a.getString(i));
        } catch (Exception e) { call.reject("urls must be strings"); return; }

        for (String u : list) {
            if (u == null || !(u.startsWith("http://") || u.startsWith("https://"))) continue;
            if (fetch(u, out)) {
                call.resolve(new JSObject().put("saved", true).put("path", out.getAbsolutePath()));
                return;
            }
        }
        call.resolve(new JSObject().put("saved", false));
    }

    /** Downloads to a neighbouring part-file first, so a cut connection cannot
        leave a half-written mp3 that looks like a finished one. */
    private boolean fetch(String url, File out) {
        File part = new File(out.getAbsolutePath() + ".part");
        HttpURLConnection c = null;
        InputStream in = null;
        OutputStream os = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setInstanceFollowRedirects(true);
            if (c.getResponseCode() / 100 != 2) return false;
            in = c.getInputStream();
            os = new FileOutputStream(part);
            byte[] buf = new byte[16384];
            int n;
            long total = 0;
            while ((n = in.read(buf)) > 0) { os.write(buf, 0, n); total += n; }
            os.flush();
            os.close(); os = null;
            if (total <= 0) { part.delete(); return false; }
            return part.renameTo(out);
        } catch (Exception e) {
            return false;
        } finally {
            try { if (os != null) os.close(); } catch (Exception ignored) { }
            try { if (in != null) in.close(); } catch (Exception ignored) { }
            if (c != null) c.disconnect();
            if (part.exists()) part.delete();
        }
    }

    /** Which of these we already hold, and where. */
    @PluginMethod
    public void have(PluginCall call) {
        String reciter = call.getString("reciter", "");
        JSArray keys = call.getArray("keys");
        JSObject paths = new JSObject();
        if (keys != null) {
            try {
                for (int i = 0; i < keys.length(); i++) {
                    String k = keys.getString(i);
                    File f = fileFor(reciter, k);
                    if (f.exists() && f.length() > 0) paths.put(k, f.getAbsolutePath());
                }
            } catch (Exception ignored) { }
        }
        call.resolve(new JSObject().put("paths", paths));
    }

    @PluginMethod
    public void remove(PluginCall call) {
        String reciter = call.getString("reciter", "");
        JSArray keys = call.getArray("keys");
        int gone = 0;
        if (keys != null) {
            try {
                for (int i = 0; i < keys.length(); i++) {
                    File f = fileFor(reciter, keys.getString(i));
                    if (f.exists() && f.delete()) gone++;
                }
            } catch (Exception ignored) { }
        }
        call.resolve(new JSObject().put("removed", gone));
    }

    /** How much is stored, so the download screen can say something true. */
    @PluginMethod
    public void stats(PluginCall call) {
        File[] fs = dir().listFiles();
        long bytes = 0;
        int n = 0;
        if (fs != null) for (File f : fs) {
            if (f.isFile() && f.getName().endsWith(".mp3")) { n++; bytes += f.length(); }
        }
        call.resolve(new JSObject().put("files", n).put("bytes", bytes));
    }
}
