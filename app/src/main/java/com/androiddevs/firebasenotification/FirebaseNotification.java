package com.androiddevs.firebasenotification;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

public class FirebaseNotification {

    private String title;
    private String message;
    private String scheduleAt;

    public FirebaseNotification() { }

    public FirebaseNotification(String title, String message, String scheduleAt) {
        this.title = title;
        this.message = message;
        this.scheduleAt = scheduleAt;
    }

    public String getTitle() { return title; }

    public String getMessage() { return message; }

    public String getScheduleAt() { return scheduleAt; }
}
