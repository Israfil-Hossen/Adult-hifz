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
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

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
 *
 * It now does this through a MediaSession, because a foreground service that
 * plays audio and offers no way to stop it is not a feature, it is a fault.
 * Three complaints came from its absence and all three are the same absence:
 * no pause anywhere, nothing on the lock screen, and a recitation that carried
 * on after the app was swiped out of recents with no control left to reach it.
 * A session gives the notification its transport buttons, puts the same
 * controls on the lock screen, makes the headset button work, and lets the
 * system stop us when it needs the audio - which is what every music app on
 * the phone already does.
 */
public class PlaybackService extends Service {

    private static final String CHANNEL = "hifz_playback";
    private static final int NOTE_ID = 1;
    static final String ACTION_STOP = "com.israfilhossen.hifz.STOP_PLAYBACK";
    static final String ACTION_PAUSE = "com.israfilhossen.hifz.PAUSE_PLAYBACK";
    static final String ACTION_PLAY  = "com.israfilhossen.hifz.PLAY_PLAYBACK";
    static final String ACTION_NEXT  = "com.israfilhossen.hifz.NEXT_AYAH";
    static final String ACTION_PREV  = "com.israfilhossen.hifz.PREV_AYAH";

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
    /* What the notification says: one line per ayah, written by the web layer
       because only it knows the surah names in the reader's language. */
    static volatile List<String> LABELS = new ArrayList<>();
    static volatile String RECITER = "";
    /* where in the current ayah we are, so a handoff back to the page does not
       replay what has already been heard */
    static volatile int POS_MS = 0;

    private MediaPlayer mp;
    private int srcTry = 0;
    private AudioFocusRequest focusReq;
    private MediaSessionCompat session;
    private boolean paused = false;
    /* set when another app takes the audio away, so we know to come back */
    private boolean duckedOut = false;

    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        switch (change) {
            case AudioManager.AUDIOFOCUS_LOSS:
                /* something else owns the audio now and means to keep it */
                stopEverything();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (!paused && mp != null) { duckedOut = true; doPause(); }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (duckedOut) { duckedOut = false; doPlay(); }
                break;
        }
    };

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
        session = new MediaSessionCompat(this, "hifz");
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlay()          { doPlay(); }
            @Override public void onPause()         { doPause(); }
            @Override public void onStop()          { stopEverything(); }
            @Override public void onSkipToNext()    { skip(1); }
            @Override public void onSkipToPrevious(){ skip(-1); }
        });
        session.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action))  { stopEverything(); return START_NOT_STICKY; }
        if (ACTION_PAUSE.equals(action)) { doPause();  return START_NOT_STICKY; }
        if (ACTION_PLAY.equals(action))  { doPlay();   return START_NOT_STICKY; }
        if (ACTION_NEXT.equals(action))  { skip(1);    return START_NOT_STICKY; }
        if (ACTION_PREV.equals(action))  { skip(-1);   return START_NOT_STICKY; }
        MediaButtonReceiver.handleIntent(session, intent);

        startForeground(NOTE_ID, buildNotification());

        if (intent != null && intent.getBooleanExtra("play", false)) {
            paused = false;
            requestFocus();
            playCurrent();
        }
        return START_NOT_STICKY;
    }

    /**
     * The app was swiped out of recents.
     *
     * A foreground service is not killed with its task, so the recitation
     * carried on with the app gone and every control gone with it - the reader
     * had nothing left to press. Clearing the app means stopping.
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopEverything();
        super.onTaskRemoved(rootIntent);
    }

    /* ---------------------------------------------------------- transport */

    private void doPlay() {
        if (mp != null && paused) {
            paused = false;
            requestFocus();
            try { mp.start(); PLAYING = true; } catch (Exception e) { onFail(); return; }
            pushState();
            return;
        }
        if (mp == null) { paused = false; requestFocus(); playCurrent(); }
    }

    private void doPause() {
        if (mp == null) return;
        try {
            if (mp.isPlaying()) mp.pause();
            POS_MS = mp.getCurrentPosition();
        } catch (Exception ignored) {}
        paused = true;
        PLAYING = false;
        pushState();
    }

    private void skip(int by) {
        int next = INDEX + by;
        if (next < 0) next = 0;
        if (next >= QUEUE.size()) {
            /* past the end is the same as finishing the pass */
            if (REPEAT != 0 && !LOOP.isEmpty()) { next = 0; QUEUE = LOOP; }
            else { stopEverything(); return; }
        }
        INDEX = next;
        srcTry = 0;
        POS_MS = 0;
        paused = false;
        playCurrent();
    }

    private void stopEverything() {
        PLAYING = false;
        DONE = true;
        REPEAT = 0;
        release();
        abandonFocus();
        if (session != null) { session.setActive(false); }
        stopSelf();
    }

    /* ------------------------------------------------------ notification */

    private PendingIntent svc(String action, int req) {
        Intent i = new Intent(this, PlaybackService.class).setAction(action);
        return PendingIntent.getService(this, req, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /** What is sounding, in the reader's own language. */
    private String currentLabel() {
        List<String> l = LABELS;
        if (l != null && INDEX >= 0 && INDEX < l.size()) {
            String s = l.get(INDEX);
            if (s != null && s.length() > 0) return s;
        }
        return getString(R.string.app_name);
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(
                this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = currentLabel();
        String who = RECITER == null ? "" : RECITER;

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle(title)
                .setContentText(who.length() > 0 ? who : getString(R.string.playback_running))
                .setSmallIcon(R.drawable.ic_stat_play)
                .setContentIntent(tap)
                .setDeleteIntent(svc(ACTION_STOP, 4))
                .setOngoing(!paused)
                .setSilent(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        b.addAction(R.drawable.ic_media_prev, getString(R.string.playback_prev), svc(ACTION_PREV, 1));
        if (paused) {
            b.addAction(R.drawable.ic_media_play, getString(R.string.playback_play), svc(ACTION_PLAY, 2));
        } else {
            b.addAction(R.drawable.ic_media_pause, getString(R.string.playback_pause), svc(ACTION_PAUSE, 2));
        }
        b.addAction(R.drawable.ic_media_next, getString(R.string.playback_next), svc(ACTION_NEXT, 3));
        b.addAction(R.drawable.ic_media_stop, getString(R.string.playback_stop), svc(ACTION_STOP, 4));

        b.setStyle(new MediaStyle()
                .setMediaSession(session.getSessionToken())
                /* the three the system shows when the shade is collapsed */
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(svc(ACTION_STOP, 4)));
        return b.build();
    }

    /** Notification, lock screen and headset all read from the session. */
    private void pushState() {
        if (session == null) return;
        session.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentLabel())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, RECITER == null ? "" : RECITER)
                .build());
        long pos = 0;
        try { if (mp != null) pos = mp.getCurrentPosition(); } catch (Exception ignored) {}
        session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(paused ? PlaybackStateCompat.STATE_PAUSED : PlaybackStateCompat.STATE_PLAYING,
                        pos, paused ? 0f : SPEED)
                .build());
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            try { nm.notify(NOTE_ID, buildNotification()); } catch (Exception ignored) {}
        }
    }

    /* ----------------------------------------------------------- playing */

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
                    .setOnAudioFocusChangeListener(focusListener)
                    .build();
            am.requestAudioFocus(focusReq);
        } else {
            am.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonFocus() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusReq != null) am.abandonAudioFocusRequest(focusReq);
        } else {
            am.abandonAudioFocus(focusListener);
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
            final int seekTo = POS_MS;
            POS_MS = 0;
            mp.setOnPreparedListener(p -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && SPEED != 1.0f) {
                        p.setPlaybackParams(p.getPlaybackParams().setSpeed(SPEED));
                    }
                    /* pick the ayah up where the page left it, not at its start */
                    if (seekTo > 0 && seekTo < p.getDuration()) p.seekTo(seekTo);
                    p.start();
                    PLAYING = true;
                    pushState();
                } catch (Exception e) { onFail(); }
            });
            mp.setOnCompletionListener(p -> { INDEX++; srcTry = 0; POS_MS = 0; playCurrent(); });
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
        abandonFocus();
        if (session != null) { session.setActive(false); session.release(); session = null; }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        super.onDestroy();
    }
}
