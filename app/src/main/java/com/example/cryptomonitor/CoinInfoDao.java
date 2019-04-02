package com.example.cryptomonitor;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CoinInfoDao {

    @Query("select * from coininfo")
    List<CoinInfo> getAll();

    @Query("select * from coininfo where coinId=:id")
    CoinInfo getById(long id);

    @Insert
    void insert(List<CoinInfo> coinInfoList);

    @Insert
    void insert(CoinInfo coinInfo);

    @Update
    void update(CoinInfo coinInfo);

    @Delete
    void delete(CoinInfo coinInfo);

}
