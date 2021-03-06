package com.example.cryptomonitor.favorite;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.coins.CoinDataSource;
import com.example.cryptomonitor.database.coins.CoinInfo;
import com.example.cryptomonitor.database.coins.CoinRepo;
import com.example.cryptomonitor.events.Event;
import com.example.cryptomonitor.events.Message;

import java.util.ArrayList;
import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {
    private CoinDataSource mCoinDataSource = new CoinRepo();
    private MutableLiveData<List<CoinInfo>> mFavoriteCoinsLiveData = new MutableLiveData<>();
    private MutableLiveData<Event> mEventMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsRefreshLiveData = new MutableLiveData<>();

    private boolean isRefreshing;

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        mCoinDataSource.getFavoriteCoins(new CoinDataSource.GetCoinCallback() {
            @Override
            public void onLoaded(List<CoinInfo> coinInfoList) {
                mFavoriteCoinsLiveData.setValue(coinInfoList);
            }

            @Override
            public void onFailed() {
                mEventMutableLiveData.setValue(new Message(getString(R.string.updating_failed)));
            }
        });
    }

    LiveData<List<CoinInfo>> getFavoriteCoinsLiveData() {
        return mFavoriteCoinsLiveData;
    }

    LiveData<Event> getEventLiveData() {
        return mEventMutableLiveData;
    }

    LiveData<Boolean> getIsRefreshLiveData() {
        return mIsRefreshLiveData;
    }

    void onStarClicked(CoinInfo clickedCoinInfo) {
        mCoinDataSource.updateCoin(clickedCoinInfo);
    }

    void onSearchClicked() {
        mFavoriteCoinsLiveData.setValue(new ArrayList<>());
    }

    void onRefresh() {
        if (!isRefreshing) {
            isRefreshing = true;
            mIsRefreshLiveData.setValue(true);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
            String currency = preferences.getString("currency", "USD");
            mCoinDataSource.refreshCoins(currency, new CoinDataSource.RefreshCallback() {
                @Override
                public void onSuccess() {
                    mEventMutableLiveData.setValue(new Message(getString(R.string.update_success)));
                    isRefreshing = false;
                    mIsRefreshLiveData.setValue(false);
                }

                @Override
                public void onFailed() {
                    mEventMutableLiveData.setValue(new Message(getString(R.string.updating_failed)));
                    isRefreshing = false;
                    mIsRefreshLiveData.setValue(false);
                }
            });
        }
    }

    void onTextChanged(String currentText) {
        mCoinDataSource.getSearchFavoriteCoins(currentText, new CoinDataSource.GetCoinCallback() {
            @Override
            public void onLoaded(List<CoinInfo> coinInfoList) {
                mFavoriteCoinsLiveData.setValue(coinInfoList);
            }

            @Override
            public void onFailed() {
                mEventMutableLiveData.setValue(new Message(getString(R.string.updating_failed)));
            }
        });
    }

    void onSearchDeactivated() {
        mCoinDataSource.getFavoriteCoins(new CoinDataSource.GetCoinCallback() {
            @Override
            public void onLoaded(List<CoinInfo> coinInfoList) {
                mFavoriteCoinsLiveData.setValue(coinInfoList);
            }

            @Override
            public void onFailed() {
                mEventMutableLiveData.setValue(new Message(getString(R.string.updating_failed)));
            }
        });
    }

    private String getString(int resId) {
        return getApplication().getString(resId);
    }
}