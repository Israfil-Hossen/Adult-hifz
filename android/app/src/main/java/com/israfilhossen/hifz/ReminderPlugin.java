package com.israfilhossen.hifz;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Calendar;

/**
 * The web layer's bridge to the daily reminder.
 *
 * set(hour, minute, title, body): hand the time to Android and forget about it.
 * cancel(): the reader turned the reminder off.
 *
 * The words come from the web layer because the app speaks five languages and
 * Android has no idea which one is chosen.
 */
@CapacitorPlugin(name = "Reminder")
public class ReminderPlugin extends Plugin {

    static PendingIntent pending(Context ctx) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(ctx, ReminderReceiver.ALARM_ID, i, flags);
    }

    /** Static so the boot receiver can put the alarm back without a bridge. */
    static void schedule(Context ctx, int hour, int minute) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Calendar at = Calendar.getInstance();
        at.set(Calendar.HOUR_OF_DAY, hour);
        at.set(Calendar.MINUTE, minute);
        at.set(Calendar.SECOND, 0);
        at.set(Calendar.MILLISECOND, 0);
        /* a time that has already passed today means tomorrow */
        if (at.getTimeInMillis() <= System.currentTimeMillis()) at.add(Calendar.DAY_OF_YEAR, 1);

        PendingIntent pi = pending(ctx);
        long when = at.getTimeInMillis();

        /* Exact alarms need a user-granted permission from Android 12, and it
           can be revoked at any time. A reminder that arrives inside a few
           minutes of the hour is a reminder; refusing to set one at all is not,
           so fall back rather than fail. */
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setWindow(AlarmManager.RTC_WAKEUP, when, 15 * 60 * 1000L, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, when, pi);
            }
        } catch (SecurityException e) {
            am.setWindow(AlarmManager.RTC_WAKEUP, when, 15 * 60 * 1000L, pi);
        }
    }

    @PluginMethod
    public void set(PluginCall call) {
        Integer hour = call.getInt("hour"), minute = call.getInt("minute");
        if (hour == null || minute == null || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            call.reject("hour and minute are required");
            return;
        }
        Context ctx = getContext();
        SharedPreferences.Editor e =
                ctx.getSharedPreferences(ReminderReceiver.PREFS, Context.MODE_PRIVATE).edit();
        e.putInt(ReminderReceiver.KEY_HOUR, hour);
        e.putInt(ReminderReceiver.KEY_MIN, minute);
        e.putString(ReminderReceiver.KEY_TITLE, call.getString("title", "Today's lesson"));
        e.putString(ReminderReceiver.KEY_BODY, call.getString("body", ""));
        e.apply();

        schedule(ctx, hour, minute);
        call.resolve(new JSObject().put("set", true));
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        Context ctx = getContext();
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.cancel(pending(ctx));
        ctx.getSharedPreferences(ReminderReceiver.PREFS, Context.MODE_PRIVATE)
           .edit().remove(ReminderReceiver.KEY_HOUR).remove(ReminderReceiver.KEY_MIN).apply();
        call.resolve(new JSObject().put("set", false));
    }

    /** Whether this phone will let us fire on the minute, so the page can say so. */
    @PluginMethod
    public void exactAllowed(PluginCall call) {
        boolean ok = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            ok = am != null && am.canScheduleExactAlarms();
        }
        call.resolve(new JSObject().put("exact", ok));
    }
}
