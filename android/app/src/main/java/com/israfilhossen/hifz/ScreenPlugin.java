package com.israfilhossen.hifz;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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

    /**
     * A file the reader asked for - a backup, a recording - saved where they
     * can find it (Download/Quran Hifz) and offered to the share sheet, so it
     * can go to Drive, WhatsApp or anywhere else.
     *
     * The page used to click an <a download> link. The WebView has no download
     * handler, so it silently did nothing: "Export backup" never made a file.
     */
    @PluginMethod
    public void share(PluginCall call) {
        String name = call.getString("name", "hifz.json");
        String mime = call.getString("mime", "application/octet-stream");
        String text = call.getString("text", null), b64 = call.getString("base64", null);
        byte[] data = null;
        try {
            if (text != null) data = text.getBytes("UTF-8");
            else if (b64 != null) data = Base64.decode(b64, Base64.DEFAULT);
        } catch (Exception ignored) { }
        if (data == null) { call.reject("nothing to save"); return; }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        String where = null;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Quran Hifz");
                Uri u = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (u != null) {
                    OutputStream os = getContext().getContentResolver().openOutputStream(u);
                    if (os != null) { os.write(data); os.close(); where = "Download/Quran Hifz/" + name; }
                }
            } catch (Exception ignored) { }
        }
        boolean shared = false;
        try {
            File dir = new File(getContext().getCacheDir(), "share");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, name);
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(data); fo.close();
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", f);
            Intent s = new Intent(Intent.ACTION_SEND).setType(mime)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent ch = Intent.createChooser(s, null);
            ch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().startActivity(ch);
            shared = true;
        } catch (Exception ignored) { }
        call.resolve(new JSObject().put("saved", where != null).put("where", where == null ? "" : where)
                .put("shared", shared));
    }
}
