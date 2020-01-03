package com.androiddevs.firebasenotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private Context context;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String notificationTitle = getInputData().getString(MainActivity.WORKER_NOTIFICATION_TITLE);
        String notificationMessage = getInputData().getString(MainActivity.WORKER_NOTIFICATION_MESSAGE);

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openMainActivity = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_alert)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(openMainActivity)
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(MainActivity.NOTIFICATION_ID, notification);

        return Result.success();
    }
}
