package com.israfilhossen.hifz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays the recitation natively while the app is in the background.
 *
 * The first version of this service only held the process alive and hoped the
 * WebView's HTML audio would keep going. It does not: once the screen is off,
 * Android may silence WebView media regardless of any foreground service. So
 * when the page goes hidden mid-recitation, the web layer hands the REMAINING
 * QUEUE of audio URLs over to this service, which plays them with MediaPlayer -
 * the same machinery every music app uses, and the only thing Android actually
 * keeps alive. When the app returns to the screen, the web layer asks where we
 * got to, takes over from that ayah, and stops us.
 *
 * Each queue entry carries every host that serves that ayah, in order; a failed
 * host falls through to the next, mirroring audioSrcs() on the web side.
 */
public class PlaybackService extends Service {

    private static final String CHANNEL = "hifz_playback";
    private static final int NOTE_ID = 1;
    static final String ACTION_STOP = "com.israfilhossen.hifz.STOP_PLAYBACK";

    /* the handoff: written by the plugin, read here. One process, one user. */
    static volatile List<List<String>> QUEUE = new ArrayList<>();
    static volatile int INDEX = 0;
    static volatile boolean PLAYING = false;
    static volatile float SPEED = 1.0f;
    /* The lesson again, and how many more times. QUEUE is the tail the page
       broke off in the middle of; LOOP is the whole lesson to repeat after it.
       REPEAT counts the passes still owed, -1 meaning endlessly - the reader
       who chose that wants the lesson in their ear all day, not until the
       first pass runs out. */
    static volatile List<List<String>> LOOP = new ArrayList<>();
    static volatile int REPEAT = 0;
    static volatile int PASSES = 0;
    static volatile long STOP_AT = 0;
    static volatile boolean DONE = false;

    private MediaPlayer mp;
    private int srcTry = 0;
    private AudioFocusRequest focusReq;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(
                    CHANNEL, getString(R.string.playback_channel), NotificationManager.IMPORTANCE_LOW);
            c.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(c);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* An endless lesson has to be stoppable without hunting for the app.
           The notification is where a listener already looks to see it playing,
           so that is where the stop belongs. */
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            PLAYING = false; DONE = true; REPEAT = 0;
            stopSelf();
            return START_NOT_STICKY;
        }
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(
                this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification note = new NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.playback_running))
                .setSmallIcon(R.drawable.ic_stat_play)
                .setContentIntent(tap)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, getString(R.string.playback_stop), stopIntent())
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTE_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTE_ID, note);
        }

        if (intent != null && intent.getBooleanExtra("play", false)) {
            requestFocus();
            playCurrent();
        }
        return START_NOT_STICKY;
    }

    private PendingIntent stopIntent() {
        Intent i = new Intent(this, PlaybackService.class).setAction(ACTION_STOP);
        return PendingIntent.getService(this, 1, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestFocus() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am == null) return;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusReq = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
                    .build();
            am.requestAudioFocus(focusReq);
        }
    }

    private void playCurrent() {
        /* the sleep timer ends the day between ayat, never mid-word */
        if (STOP_AT > 0 && System.currentTimeMillis() >= STOP_AT) {
            PLAYING = false; DONE = true; stopSelf(); return;
        }
        List<List<String>> q = QUEUE;
        if (INDEX >= q.size()) {
            if (REPEAT != 0 && !LOOP.isEmpty()) {
                if (REPEAT > 0) REPEAT--;
                PASSES++;
                QUEUE = LOOP;
                INDEX = 0; srcTry = 0;
                playCurrent();
                return;
            }
            PLAYING = false; DONE = true; stopSelf(); return;
        }
        List<String> srcs = q.get(INDEX);
        if (srcTry >= srcs.size()) {
            /* every host refused this ayah - skip it rather than fall silent */
            INDEX++; srcTry = 0; playCurrent(); return;
        }
        release();
        mp = new MediaPlayer();
        mp.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            mp.setDataSource(srcs.get(srcTry));
            mp.setOnPreparedListener(p -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && SPEED != 1.0f) {
                        p.setPlaybackParams(p.getPlaybackParams().setSpeed(SPEED));
                    }
                    p.start();
                    PLAYING = true;
                } catch (Exception e) { onFail(); }
            });
            mp.setOnCompletionListener(p -> { INDEX++; srcTry = 0; playCurrent(); });
            mp.setOnErrorListener((p, a, b) -> { onFail(); return true; });
            mp.prepareAsync();
        } catch (Exception e) { onFail(); }
    }

    private void onFail() { srcTry++; playCurrent(); }

    private void release() {
        if (mp != null) {
            try { mp.release(); } catch (Exception ignored) {}
            mp = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        PLAYING = false;
        release();
        if (focusReq != null) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (am != null) am.abandonAudioFocusRequest(focusReq);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        super.onDestroy();
    }
}
