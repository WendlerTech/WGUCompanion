package tech.wendler.wgucompanion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context));
        SharedPreferences.Editor sharedPrefEditor = prefs.edit();

        String title = "Placeholder Title", content = "Placeholder Content";

        Bundle notifyBundle = intent.getExtras();
        if (notifyBundle != null) {
            title = notifyBundle.getString("title");
            content = notifyBundle.getString("content");
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, ViewTerms.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CourseNotifyID",
                    "Course Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Handles notifications about course start and end dates.");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CourseNotifyID");
        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
            Calendar nextNotifyTime = Calendar.getInstance();
            nextNotifyTime.add(Calendar.DATE, 1);
            sharedPrefEditor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
            sharedPrefEditor.apply();
        }
    }
}