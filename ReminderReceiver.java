package com.jovid.habit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    String PREFS_NAME = "HabitPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int streak = settings.getInt("streak", 0);
        String habitName = settings.getString("habit_name", "عادتت");
        int count = settings.getInt("saved_count", 0);

        String title = "⏰ یادت نره!";
        String message;

        if (streak > 0) {
            message = "🔥 " + streak + " روز پشت‌سرهم! نذار زنجیره‌ت بشکنه!";
        } else if (count > 0) {
            message = "برگرد و " + habitName + " رو ادامه بده 💪";
        } else {
            message = "یادت نره امروز " + habitName + " رو انجام بدی 💪";
        }

        showNotification(context, title, message);
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager nm = (NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "habit_reminder";
        String channelName = "یادآوری عادت";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("یادآوری روزانه عادت");
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
            | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent, flags);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setSmallIcon(android.R.drawable.ic_popup_reminder)
               .setContentTitle(title)
               .setContentText(message)
               .setStyle(new Notification.BigTextStyle().bigText(message))
               .setContentIntent(pendingIntent)
               .setAutoCancel(true)
               .setDefaults(Notification.DEFAULT_ALL);

        if (nm != null) {
            nm.notify(1001, builder.build());
        }
    }
}