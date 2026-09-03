package com.israfilhossen.hifz;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

/**
 * The dhikr, said aloud from a file inside the app.
 *
 * The web layer used to speak it with speechSynthesis. That needs an Arabic
 * voice installed on the phone, which most phones do not have, so the reminder
 * arrived silent far more often than not - and it could not make a sound at all
 * once the app was closed, which is when a dhikr reminder is worth having.
 *
 * A recording bundled in the app has neither problem: it needs no network, no
 * installed voice, and MediaPlayer will play it with the screen off. The files
 * live beside the rest of the web assets (assets/public/zikr/) so they travel
 * with the app and are copied by the ordinary build.
 *
 * A missing file is not an error. The notification still arrives, with the
 * phone's own sound - which is exactly what happens today.
 */
final class ZikrSound {

    private static MediaPlayer mp;

    static boolean play(Context ctx, String file) {
        if (file == null || file.length() == 0) return false;
        /* the name comes from our own table, but it addresses the filesystem:
           anything with a path in it is refused rather than trusted */
        if (file.contains("/") || file.contains("..")) return false;
        AssetFileDescriptor fd = null;
        try {
            fd = ctx.getAssets().openFd("public/zikr/" + file);
            release();
            mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mp.setOnCompletionListener(p -> release());
            mp.setOnErrorListener((p, a, b) -> { release(); return true; });
            mp.prepare();
            mp.start();
            return true;
        } catch (Exception e) {
            release();
            return false;
        } finally {
            if (fd != null) try { fd.close(); } catch (Exception ignored) {}
        }
    }

    private static void release() {
        if (mp != null) {
            try { mp.release(); } catch (Exception ignored) {}
            mp = null;
        }
    }

    private ZikrSound() {}
}
