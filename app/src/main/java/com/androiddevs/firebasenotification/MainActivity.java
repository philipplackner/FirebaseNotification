package com.androiddevs.firebasenotification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_NAME = "channelName";
    private static final String CHANNEL_DESCRIPTION = "This is my channel description";

    public static final String CHANNEL_ID = "myChannel";
    public static final int NOTIFICATION_ID = 0;

    public static final String WORKER_NOTIFICATION_TITLE = "WORKER_NOTIFICATION_TITLE";
    public static final String WORKER_NOTIFICATION_MESSAGE = "WORKER_NOTIFICATION_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        fetchNewNotificationsFromFirebase();
    }

    private void fetchNewNotificationsFromFirebase() {
        CollectionReference notificationCollection = FirebaseFirestore.getInstance()
                .collection("notifications");

        notificationCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots == null) {
                    return;
                }
                WorkManager.getInstance(getApplicationContext()).cancelAllWork();
                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    FirebaseNotification notification = snapshot.toObject(FirebaseNotification.class);
                    try {
                        scheduleNotification(notification);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void scheduleNotification(FirebaseNotification notification) throws ParseException{
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String scheduleAt = notification.getScheduleAt();
        Date scheduleDate = dateFormat.parse(scheduleAt);
        scheduleWorker(scheduleDate, notification);
    }

    private void scheduleWorker(Date date, FirebaseNotification notification) {
        long delay = date.getTime() - System.currentTimeMillis();
        if(delay < 0) {
            return;
        }
        Data inputData = new Data.Builder()
                .putString(WORKER_NOTIFICATION_TITLE, notification.getTitle())
                .putString(WORKER_NOTIFICATION_MESSAGE, notification.getMessage())
                .build();

        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest
                .Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager manager = WorkManager.getInstance(this);
        manager.beginUniqueWork(String.valueOf(new Random().nextInt()), ExistingWorkPolicy.KEEP,
                notificationWorkRequest).enqueue();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.setLightColor(Color.argb(255, 0, 255, 255));
            channel.enableLights(true);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }


}
