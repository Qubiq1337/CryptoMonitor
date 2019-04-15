package com.example.cryptomonitor.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.cryptomonitor.database.dao.CoinInfoDao;
import com.example.cryptomonitor.database.entities.CoinInfo;

@Database(entities = {CoinInfo.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CoinInfoDao coinInfoDao();
}
