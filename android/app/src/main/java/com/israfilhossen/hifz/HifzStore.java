package com.israfilhossen.hifz;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Where a download lives, and the one way anything gets downloaded.
 *
 * Recitation used to go into the app's private folder. That survives an update,
 * but an uninstall wipes it - and so does installing a build signed with a
 * different key, which Android will only do after an uninstall. A reader who
 * had pulled down a whole juz lost all of it the first time that happened.
 *
 * So the audio now goes where the phone keeps music: Music/Adult Hifz/<reciter>/.
 * It stays there whatever happens to the app. A reinstall can read it again
 * with one permission, and nothing has to come down twice.
 *
 * Page fonts cannot go there - that folder accepts audio and nothing else - so
 * they stay private. They are small, and come back on their own.
 */
final class HifzStore {

    private HifzStore() { }

    static File musicRoot() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "Adult Hifz");
    }

    static boolean mounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /** From Android 11 an app may create audio in Music/ with no permission at
        all. Before that it needs the old write permission. */
    static boolean canWriteShared(Context c) {
        if (!mounted()) return false;
        if (Build.VERSION.SDK_INT >= 30) return true;
        return ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Reading files this install did not write - the ones a previous install
        left behind - is what needs asking for. */
    static String readPermission() {
        return Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    static boolean canReadShared(Context c) {
        if (Build.VERSION.SDK_INT < 23) return true;
        return ContextCompat.checkSelfPermission(c, readPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    static String safe(String reciter) {
        String r = reciter == null ? "" : reciter.replaceAll("[^A-Za-z0-9._-]", "_");
        return r.length() == 0 ? "default" : r;
    }

    /** "2:255" becomes 002255.mp3 - the name every recitation site uses. */
    static String fileName(String key) {
        String[] p = (key == null ? "" : key).split(":");
        try {
            return String.format(java.util.Locale.US, "%03d%03d.mp3",
                    Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
        } catch (Exception e) {
            return (key == null ? "x" : key.replaceAll("[^0-9]", "_")) + ".mp3";
        }
    }

    static File sharedDir(String reciter) {
        return new File(musicRoot(), safe(reciter));
    }

    static File sharedFile(String reciter, String key) {
        return new File(sharedDir(reciter), fileName(key));
    }

    static File privateDir(Context c) {
        File d = new File(c.getFilesDir(), "recite");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    /** the name the previous version used, kept so its files are still found */
    static File privateFile(Context c, String reciter, String key) {
        String k = (key == null ? "" : key).replaceAll("[^0-9]", "_");
        String r = reciter == null ? "" : reciter.replaceAll("[^A-Za-z0-9._-]", "_");
        return new File(privateDir(c), r + "__" + k + ".mp3");
    }

    static boolean ok(File f) {
        try { return f != null && f.isFile() && f.length() > 0; } catch (Exception e) { return false; }
    }

    /** where this ayah already is, or null */
    static File existing(Context c, String reciter, String key) {
        File s = sharedFile(reciter, key);
        if (ok(s)) return s;
        File p = privateFile(c, reciter, key);
        if (ok(p)) return p;
        return null;
    }

    /** where a new download of this ayah should go */
    static File target(Context c, String reciter, String key) {
        if (canWriteShared(c)) {
            File d = sharedDir(reciter);
            if (d.isDirectory() || d.mkdirs()) return new File(d, fileName(key));
        }
        return privateFile(c, reciter, key);
    }

    static File fontFile(Context c, String v, int page) {
        File d = new File(new File(c.getFilesDir(), "qcf"), safe(v));
        if (!d.exists()) d.mkdirs();
        return new File(d, "p" + page + ".woff2");
    }

    /**
     * Fetches into the cache first and only then moves the file into place, so
     * a cut connection never leaves half an mp3 that looks like a whole one.
     * (It cannot be written beside the final file: Music/ refuses anything that
     * does not end in an audio extension, a ".part" file included.)
     */
    static boolean fetch(Context c, String url, File out) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) return false;
        File tmpDir = new File(c.getCacheDir(), "dl");
        if (!tmpDir.exists()) tmpDir.mkdirs();
        File part = new File(tmpDir, UUID.randomUUID().toString() + ".part");
        HttpURLConnection conn = null;
        InputStream in = null;
        OutputStream os = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setInstanceFollowRedirects(true);
            if (conn.getResponseCode() / 100 != 2) return false;
            in = conn.getInputStream();
            os = new FileOutputStream(part);
            byte[] buf = new byte[16384];
            int n;
            long total = 0;
            while ((n = in.read(buf)) > 0) {
                if (Thread.currentThread().isInterrupted()) return false;
                os.write(buf, 0, n);
                total += n;
            }
            os.flush();
            os.close(); os = null;
            if (total <= 0) return false;
            return place(part, out);
        } catch (Exception e) {
            return false;
        } finally {
            try { if (os != null) os.close(); } catch (Exception ignored) { }
            try { if (in != null) in.close(); } catch (Exception ignored) { }
            if (conn != null) conn.disconnect();
            if (part.exists()) part.delete();
        }
    }

    /** a rename where the two are on one disk, a copy where they are not */
    static boolean place(File from, File to) {
        File dir = to.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        if (from.renameTo(to)) return true;
        InputStream in = null;
        OutputStream os = null;
        boolean done = false;
        try {
            in = new FileInputStream(from);
            os = new FileOutputStream(to);
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            os.flush();
            done = true;
        } catch (Exception e) {
            done = false;
        } finally {
            try { if (os != null) os.close(); } catch (Exception ignored) { }
            try { if (in != null) in.close(); } catch (Exception ignored) { }
            if (!done) to.delete();
        }
        return done && ok(to);
    }

    /**
     * The files the previous version kept privately go out to Music/, once,
     * where the next uninstall cannot reach them. Anything that will not move
     * stays where it was and is still found there.
     */
    static void migrate(Context c) {
        if (!canWriteShared(c)) return;
        File[] fs = privateDir(c).listFiles();
        if (fs == null) return;
        for (File f : fs) {
            String name = f.getName();
            int cut = name.indexOf("__");
            if (!f.isFile() || cut < 0 || !name.endsWith(".mp3")) continue;
            String rec = name.substring(0, cut);
            String[] k = name.substring(cut + 2, name.length() - 4).split("_");
            if (k.length != 2) continue;
            String key = k[0] + ":" + k[1];
            File dest = sharedFile(rec.length() == 0 ? "default" : rec, key);
            if (ok(dest)) { f.delete(); continue; }
            File d = dest.getParentFile();
            if (d != null && !d.isDirectory() && !d.mkdirs()) return;
            if (place(f, dest)) f.delete();
        }
    }
}
