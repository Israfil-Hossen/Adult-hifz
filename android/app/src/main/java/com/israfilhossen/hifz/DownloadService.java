package com.israfilhossen.hifz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A download that belongs to the phone, not to the screen.
 *
 * It used to run inside the page: a loop in the web view, one file after the
 * next. Press Back and there was nowhere to go but out of it; press Home and
 * Android froze the app within seconds, because nothing said it was still
 * working - and a while later killed it, which the reader saw as a crash that
 * reopened on some other screen.
 *
 * Now the page hands the whole list over and this runs it, in the open, with
 * its own line in the notification shade and its own Stop. The app can go to
 * the background, or be swiped away, and the files keep coming.
 */
public class DownloadService extends Service {

    static final String ACTION_STOP = "com.israfilhossen.hifz.DL_STOP";
    private static final String CHANNEL = "hifz_download";
    private static final int NOTE_ID = 7402;
    private static final int DONE_ID = 7403;
    private static final String PREFS = "hifz_dl";
    private static final int THREADS = 3;

    static final class Task {
        final boolean font;
        final String key;
        final int page;
        final String[] urls;
        Task(boolean font, String key, int page, String[] urls) {
            this.font = font; this.key = key; this.page = page; this.urls = urls;
        }
    }

    static final class Job {
        String id = "", reciter = "", title = "", meta = "";
        String ing = "", done = "", stopped = "", stopLabel = "", missed = "";
        List<Integer> pages = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        final AtomicInteger finished = new AtomicInteger();
        final AtomicInteger audioMissed = new AtomicInteger();
        final AtomicInteger pageMissed = new AtomicInteger();
        /* per page: tasks left, font failed, audio failed */
        final Map<Integer, int[]> per = new HashMap<>();
        volatile boolean stop;
        volatile String state = "running";
    }

    /* one download at a time; the page asks before it hands over another */
    static volatile Job current;
    private static volatile Job pending;

    private ExecutorService pool;
    private PowerManager.WakeLock wake;
    private long lastNote;

    static boolean busy() {
        Job j = current;
        return j != null && "running".equals(j.state);
    }

    static void start(Context c, Job j) {
        pending = j;
        Intent i = new Intent(c, DownloadService.class);
        ContextCompat.startForegroundService(c, i);
    }

    static void requestStop(Context c) {
        Job j = current;
        if (j != null) j.stop = true;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, getString(R.string.download_channel), NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            Job j = current;
            if (j != null) j.stop = true;
            if (j == null || !"running".equals(j.state)) stopSelf();
            return START_NOT_STICKY;
        }
        Job j = pending;
        pending = null;
        if (busy()) return START_NOT_STICKY;      /* one at a time; the page asks first */
        if (j == null) {
            /* nothing to do - but Android is still owed the foreground promise */
            goForeground(null);
            finishService();
            return START_NOT_STICKY;
        }
        current = j;
        goForeground(j);
        run(j);
        return START_NOT_STICKY;
    }

    private void goForeground(Job j) {
        Notification n = build(j);
        int type = 0;
        if (Build.VERSION.SDK_INT >= 29) type = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
        try {
            ServiceCompat.startForeground(this, NOTE_ID, n, type);
        } catch (Exception e) {
            /* the system can refuse (a background start on a strict phone); the
               download still runs for as long as the app is in front */
        }
    }

    private void run(final Job j) {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hifz:download");
                wake.setReferenceCounted(false);
                wake.acquire(6 * 60 * 60 * 1000L);
            }
        } catch (Exception ignored) { }

        for (Integer p : j.pages) j.per.put(p, new int[]{0, 0, 0});
        for (Task t : j.tasks) {
            int[] a = j.per.get(t.page);
            if (a == null) { a = new int[]{0, 0, 0}; j.per.put(t.page, a); }
            a[0]++;
        }
        save(j);

        pool = Executors.newFixedThreadPool(THREADS);
        for (final Task t : j.tasks) {
            pool.execute(() -> {
                if (j.stop) return;
                boolean got = doTask(j, t);
                /* cut off by Stop is not the same as missing: leave the page
                   unfinished rather than write it down as broken */
                if (!got && j.stop) return;
                synchronized (j) {
                    int[] a = j.per.get(t.page);
                    if (a != null) {
                        a[0]--;
                        if (!got) { if (t.font) a[1] = 1; else a[2]++; }
                    }
                    if (!got) { if (t.font) j.pageMissed.incrementAndGet(); else j.audioMissed.incrementAndGet(); }
                }
                int f = j.finished.incrementAndGet();
                tick(j, f);
            });
        }
        pool.shutdown();
        new Thread(() -> {
            try {
                while (!pool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    if (j.stop) { pool.shutdownNow(); }
                }
            } catch (InterruptedException ignored) { }
            j.state = j.stop ? "stopped" : "done";
            save(j);
            AudioStorePlugin.tellDownload(snapshot(j));
            announce(j);
            finishService();
        }, "hifz-dl-wait").start();
    }

    private boolean doTask(Job j, Task t) {
        File out;
        if (t.font) {
            String[] vp = t.key.split("/");
            out = HifzStore.fontFile(this, vp[0], t.page);
            if (HifzStore.ok(out)) return true;
        } else {
            if (HifzStore.existing(this, j.reciter, t.key) != null) return true;
            out = HifzStore.target(this, j.reciter, t.key);
        }
        for (String u : t.urls) {
            if (j.stop) return false;
            if (HifzStore.fetch(this, u, out)) return true;
        }
        return false;
    }

    private void tick(Job j, int f) {
        long now = System.currentTimeMillis();
        boolean last = f >= j.tasks.size();
        if (!last && now - lastNote < 700) return;
        lastNote = now;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && !j.stop) {
            try { nm.notify(NOTE_ID, build(j)); } catch (Exception ignored) { }
        }
        /* every page that finishes is written down, so even a download the
           system cut short still counts what it got */
        save(j);
        AudioStorePlugin.tellDownload(snapshot(j));
    }

    private PendingIntent openApp() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 11, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification build(Job j) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_download)
                .setContentIntent(openApp())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS);
        if (j == null) {
            return b.setContentTitle(getString(R.string.app_name)).build();
        }
        int total = Math.max(1, j.tasks.size()), f = j.finished.get();
        int pct = Math.round(f * 100f / total);
        b.setContentTitle(j.title.length() > 0 ? j.title : getString(R.string.app_name))
         .setContentText(j.ing + " · " + pct + "%  (" + f + " / " + j.tasks.size() + ")")
         .setProgress(total, f, false);
        Intent s = new Intent(this, DownloadService.class).setAction(ACTION_STOP);
        PendingIntent stop = PendingIntent.getService(this, 12, s,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        b.addAction(R.drawable.ic_media_stop,
                j.stopLabel.length() > 0 ? j.stopLabel : getString(R.string.playback_stop), stop);
        return b.build();
    }

    /** the last word: a plain notification that stays until it is read */
    private void announce(Job j) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;
        String text = "stopped".equals(j.state) ? j.stopped : j.done;
        int miss = j.audioMissed.get() + j.pageMissed.get();
        if (!"stopped".equals(j.state) && miss > 0 && j.missed.length() > 0) {
            text = text + " · " + j.missed.replace("{0}", String.valueOf(miss));
        }
        Notification n = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_download)
                .setContentTitle(j.title.length() > 0 ? j.title : getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(openApp())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        try { nm.notify(DONE_ID, n); } catch (Exception ignored) { }
    }

    private void finishService() {
        try { if (wake != null && wake.isHeld()) wake.release(); } catch (Exception ignored) { }
        wake = null;
        try { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE); } catch (Exception ignored) { }
        stopSelf();
    }

    /** Android 15 gives a data transfer six hours a day; past that, stop cleanly
        and keep what arrived rather than be killed mid-file */
    @Override
    public void onTimeout(int startId, int fgsType) {
        Job j = current;
        if (j != null) j.stop = true;
        if (pool != null) pool.shutdownNow();
    }

    @Override
    public void onTimeout(int startId) {
        onTimeout(startId, 0);
    }

    @Override
    public void onDestroy() {
        Job j = current;
        if (j != null && "running".equals(j.state)) {
            j.stop = true;
            if (pool != null) pool.shutdownNow();
        }
        try { if (wake != null && wake.isHeld()) wake.release(); } catch (Exception ignored) { }
        super.onDestroy();
    }

    /* ---- what the page reads back ---- */

    static JSONObject snapshot(Job j) {
        JSONObject o = new JSONObject();
        try {
            o.put("id", j.id);
            o.put("state", j.state);
            o.put("total", j.tasks.size());
            o.put("done", j.finished.get());
            o.put("audioMissed", j.audioMissed.get());
            o.put("pageMissed", j.pageMissed.get());
            o.put("title", j.title);
            o.put("meta", j.meta);
            JSONObject per = new JSONObject();
            synchronized (j) {
                for (Map.Entry<Integer, int[]> e : j.per.entrySet()) {
                    int[] a = e.getValue();
                    if (a[0] > 0) continue;              /* not finished yet */
                    JSONObject pp = new JSONObject();
                    pp.put("page", a[1]);
                    pp.put("audio", a[2]);
                    per.put(String.valueOf(e.getKey()), pp);
                }
            }
            o.put("perPage", per);
        } catch (Exception ignored) { }
        return o;
    }

    private void save(Job j) {
        saveTo(this, snapshot(j));
    }

    static void saveTo(Context c, JSONObject o) {
        try {
            SharedPreferences sp = c.getSharedPreferences(PREFS, MODE_PRIVATE);
            sp.edit().putString("last", o.toString()).apply();
        } catch (Exception ignored) { }
    }

    /** the running job, or the last one the page has not yet acknowledged. A
        job the system killed is reported as stopped, with what it got. */
    static JSONObject status(Context c) {
        Job j = current;
        if (j != null && "running".equals(j.state)) return snapshot(j);
        try {
            SharedPreferences sp = c.getSharedPreferences(PREFS, MODE_PRIVATE);
            String s = sp.getString("last", null);
            if (s == null) return new JSONObject().put("state", "none");
            JSONObject o = new JSONObject(s);
            if ("running".equals(o.optString("state"))) o.put("state", "stopped");
            return o;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    static void ack(Context c, String id) {
        try {
            SharedPreferences sp = c.getSharedPreferences(PREFS, MODE_PRIVATE);
            String s = sp.getString("last", null);
            if (s == null) return;
            JSONObject o = new JSONObject(s);
            if (id == null || id.equals(o.optString("id"))) sp.edit().remove("last").apply();
        } catch (Exception ignored) { }
    }
}
