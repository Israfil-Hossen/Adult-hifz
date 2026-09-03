package com.israfilhossen.hifz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * The daily nudge, posted by Android rather than by the page.
 *
 * The web layer had a reminder already: a setInterval that checked the clock
 * every thirty seconds. It only ever fired while the app was open and looking
 * at the reminder - which is the one moment nobody needs reminding. Closed,
 * backgrounded or asleep, the timer does not run, so the reminder a reader set
 * for nine in the evening simply never arrived.
 *
 * This is an alarm the system holds. It survives the app being closed and,
 * through {@link #onReceive}'s BOOT_COMPLETED branch, the phone being
 * restarted. The web layer only tells us the hour and minute.
 */
public class ReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL   = "hifz-daily";
    static final String PREFS     = "hifz-reminder";
    static final String KEY_HOUR  = "hour";
    static final String KEY_MIN   = "minute";
    static final String KEY_TITLE = "title";
    static final String KEY_BODY  = "body";
    static final int    ALARM_ID  = 7311;
    static final int    NOTE_ID   = 7312;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent == null ? null : intent.getAction();

        /* a restart clears every alarm the system was holding, so put ours back */
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.MY_PACKAGE_REPLACED".equals(action)) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            int h = p.getInt(KEY_HOUR, -1), m = p.getInt(KEY_MIN, -1);
            if (h >= 0 && m >= 0) ReminderPlugin.schedule(ctx, h, m);
            return;
        }

        show(ctx);
        /* an exact alarm fires once; ask for tomorrow's before this one ends */
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int h = p.getInt(KEY_HOUR, -1), m = p.getInt(KEY_MIN, -1);
        if (h >= 0 && m >= 0) ReminderPlugin.schedule(ctx, h, m);
    }

    boolean post(Context ctx) { return show(ctx); }

    private boolean show(Context ctx) {
        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, "Daily lesson", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("A reminder at the time you chose");
            nm.createNotificationChannel(ch);
        }

        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String title = p.getString(KEY_TITLE, "Today's lesson");
        String body  = p.getString(KEY_BODY, "");

        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, flags);

        Notification.Builder b = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? new Notification.Builder(ctx, CHANNEL)
                : new Notification.Builder(ctx);

        b.setSmallIcon(R.drawable.ic_stat_play)
         .setContentTitle(title)
         .setContentIntent(pi)
         .setAutoCancel(true);
        if (body != null && body.length() > 0) {
            b.setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body));
        }

        try { nm.notify(NOTE_ID, b.build()); return true; }
        catch (SecurityException e) { return false; }
    }
}
