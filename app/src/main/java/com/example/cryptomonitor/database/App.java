package com.example.cryptomonitor.database;

import android.app.Application;
import android.arch.persistence.room.Room;

public class App extends Application {

    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}