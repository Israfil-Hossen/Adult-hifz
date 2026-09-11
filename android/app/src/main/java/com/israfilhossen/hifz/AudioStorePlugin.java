package com.israfilhossen.hifz;

import android.Manifest;
import android.os.Build;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Where downloaded recitation actually lives, and the page's way of asking for
 * more of it.
 *
 * The files are real files (see {@link HifzStore}): the page plays them through
 * Capacitor.convertFileSrc and the native player opens the same path when the
 * screen goes dark. The downloading itself is {@link DownloadService}'s - this
 * only hands it the list and reports back.
 */
@CapacitorPlugin(
    name = "AudioStore",
    permissions = {
        @Permission(strings = { Manifest.permission.READ_MEDIA_AUDIO }, alias = "audio"),
        @Permission(strings = { Manifest.permission.READ_EXTERNAL_STORAGE }, alias = "storage")
    }
)
public class AudioStorePlugin extends Plugin {

    private static volatile AudioStorePlugin self;

    @Override
    public void load() {
        self = this;
        /* the previous version's private files move out to Music/, where the
           next uninstall cannot take them; off the main thread, it can be large */
        new Thread(() -> {
            try { HifzStore.migrate(getContext()); } catch (Exception ignored) { }
        }, "hifz-migrate").start();
    }

    /** progress from the service, pushed to the page while it is listening */
    static void tellDownload(JSONObject o) {
        AudioStorePlugin p = self;
        if (p == null || o == null) return;
        try { p.notifyListeners("download", JSObject.fromJSONObject(o)); } catch (Exception ignored) { }
    }

    @PluginMethod
    public void where(PluginCall call) {
        call.resolve(new JSObject()
                .put("files", getContext().getFilesDir().getAbsolutePath())
                .put("music", HifzStore.musicRoot().getAbsolutePath())
                .put("shared", HifzStore.canWriteShared(getContext())));
    }

    /* ---- one ayah, now (kept for anything that still asks this way) ---- */

    @PluginMethod
    public void save(PluginCall call) {
        String key = call.getString("key"), reciter = call.getString("reciter", "");
        JSArray urls = call.getArray("urls");
        if (key == null || urls == null) { call.reject("key and urls are required"); return; }

        File have = HifzStore.existing(getContext(), reciter, key);
        if (have != null) {
            call.resolve(new JSObject().put("saved", true).put("path", have.getAbsolutePath()));
            return;
        }
        File out = HifzStore.target(getContext(), reciter, key);
        try {
            JSONArray a = urls;
            for (int i = 0; i < a.length(); i++) {
                if (HifzStore.fetch(getContext(), a.getString(i), out)) {
                    call.resolve(new JSObject().put("saved", true).put("path", out.getAbsolutePath()));
                    return;
                }
            }
        } catch (Exception e) { call.reject("urls must be strings"); return; }
        call.resolve(new JSObject().put("saved", false));
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
                    File f = HifzStore.existing(getContext(), reciter, k);
                    if (f != null) paths.put(k, f.getAbsolutePath());
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
                    String k = keys.getString(i);
                    File s = HifzStore.sharedFile(reciter, k);
                    File p = HifzStore.privateFile(getContext(), reciter, k);
                    File l = new File(new File(HifzStore.legacyRoot(), HifzStore.safe(reciter)), HifzStore.fileName(k));
                    if (s.exists() && s.delete()) gone++;
                    if (l.exists() && l.delete()) gone++;
                    if (p.exists() && p.delete()) gone++;
                }
            } catch (Exception ignored) { }
        }
        call.resolve(new JSObject().put("removed", gone));
    }

    /** How much is stored, so the download screen can say something true. */
    @PluginMethod
    public void stats(PluginCall call) {
        long[] t = new long[2];
        count(HifzStore.privateDir(getContext()), t, false);
        count(HifzStore.musicRoot(), t, true);
        count(HifzStore.legacyRoot(), t, true);
        call.resolve(new JSObject().put("files", t[0]).put("bytes", t[1]));
    }

    private static void count(File dir, long[] t, boolean deep) {
        File[] fs = dir.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) { if (deep) count(f, t, false); }
            else if (f.getName().endsWith(".mp3")) { t[0]++; t[1] += f.length(); }
        }
    }

    /* ---- the download that runs without the screen ---- */

    @PluginMethod
    public void start(PluginCall call) {
        if (DownloadService.busy()) {
            call.resolve(new JSObject().put("started", false).put("busy", true));
            return;
        }
        DownloadService.Job j = new DownloadService.Job();
        j.id = call.getString("id", String.valueOf(System.currentTimeMillis()));
        j.reciter = call.getString("reciter", "");
        j.title = call.getString("title", "");
        j.meta = call.getString("meta", "");
        JSObject txt = call.getObject("txt", new JSObject());
        j.ing = txt.optString("ing", "");
        j.done = txt.optString("done", "");
        j.stopped = txt.optString("stopped", "");
        j.stopLabel = txt.optString("stop", "");
        j.missed = txt.optString("missed", "");
        try {
            JSArray pages = call.getArray("pages");
            if (pages != null) for (int i = 0; i < pages.length(); i++) j.pages.add(pages.getInt(i));
            JSArray tasks = call.getArray("tasks");
            if (tasks != null) for (int i = 0; i < tasks.length(); i++) {
                JSONObject t = tasks.getJSONObject(i);
                JSONArray u = t.optJSONArray("urls");
                List<String> us = new ArrayList<>();
                if (u != null) for (int k = 0; k < u.length(); k++) us.add(u.getString(k));
                j.tasks.add(new DownloadService.Task(
                        "f".equals(t.optString("k")), t.optString("key"), t.optInt("page"),
                        us.toArray(new String[0])));
            }
        } catch (Exception e) {
            call.reject("bad job");
            return;
        }
        DownloadService.ack(getContext(), null);
        DownloadService.start(getContext(), j);
        call.resolve(new JSObject().put("started", true));
    }

    @PluginMethod
    public void status(PluginCall call) {
        try {
            call.resolve(JSObject.fromJSONObject(DownloadService.status(getContext())));
        } catch (Exception e) {
            call.resolve(new JSObject().put("state", "none"));
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        DownloadService.requestStop(getContext());
        call.resolve();
    }

    @PluginMethod
    public void ack(PluginCall call) {
        DownloadService.ack(getContext(), call.getString("id"));
        call.resolve();
    }

    /* ---- a reinstall finding what the last install left in Music/ ---- */

    /** Whether there is an Adult Hifz folder, whether we may read it, and how
        many of its files we can already see. A fresh install sees the folder
        but not the files in it until the reader says yes. */
    @PluginMethod
    public void previous(PluginCall call) {
        File root = HifzStore.musicRoot(), old = HifzStore.legacyRoot();
        long[] t = new long[2];
        count(root, t, true);
        count(old, t, true);
        call.resolve(new JSObject()
                .put("folder", root.isDirectory() || old.isDirectory())
                .put("canRead", HifzStore.canReadShared(getContext()))
                .put("visible", t[0]));
    }

    private String readAlias() {
        return Build.VERSION.SDK_INT >= 33 ? "audio" : "storage";
    }

    @PluginMethod
    public void askRead(PluginCall call) {
        if (HifzStore.canReadShared(getContext())) {
            call.resolve(new JSObject().put("granted", true));
            return;
        }
        requestPermissionForAlias(readAlias(), call, "readDone");
    }

    @PermissionCallback
    private void readDone(PluginCall call) {
        boolean ok = getPermissionState(readAlias()) == PermissionState.GRANTED
                || HifzStore.canReadShared(getContext());
        call.resolve(new JSObject().put("granted", ok));
    }

    /** every ayah of this reciter the phone holds, wherever it is */
    @PluginMethod
    public void scan(PluginCall call) {
        String reciter = call.getString("reciter", "");
        JSArray keys = new JSArray();
        List<File> all = new ArrayList<>();
        File[] a1 = HifzStore.sharedDir(reciter).listFiles();
        File[] a2 = new File(HifzStore.legacyRoot(), HifzStore.safe(reciter)).listFiles();
        if (a1 != null) java.util.Collections.addAll(all, a1);
        if (a2 != null) java.util.Collections.addAll(all, a2);
        for (File f : all) {
            String n = f.getName();
            if (n.length() == 10 && n.endsWith(".mp3") && f.length() > 0) {
                try {
                    int s = Integer.parseInt(n.substring(0, 3)), a = Integer.parseInt(n.substring(3, 6));
                    keys.put(s + ":" + a);
                } catch (Exception ignored) { }
            }
        }
        String pre = (reciter == null ? "" : reciter.replaceAll("[^A-Za-z0-9._-]", "_")) + "__";
        File[] ps = HifzStore.privateDir(getContext()).listFiles();
        if (ps != null) for (File f : ps) {
            String n = f.getName();
            if (n.startsWith(pre) && n.endsWith(".mp3") && f.length() > 0) {
                String[] k = n.substring(pre.length(), n.length() - 4).split("_");
                if (k.length == 2) keys.put(k[0] + ":" + k[1]);
            }
        }
        call.resolve(new JSObject().put("keys", keys));
    }
}
