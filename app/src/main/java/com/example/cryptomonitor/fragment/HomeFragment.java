package com.example.cryptomonitor.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.adapters.CoinAdapterHome;
import com.example.cryptomonitor.database.App;
import com.example.cryptomonitor.database.CoinInfo;
import com.example.cryptomonitor.model.CoinCryptoCompare;
import com.example.cryptomonitor.network_api.Network;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private CoinCryptoCompare mCoinCryptoCompare = new CoinCryptoCompare();
    private RecyclerView mRecyclerView;
    CoinAdapterHome mCoinAdapterHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout, container, false);
        mRecyclerView = view.findViewById(R.id.rv_coin_itemlist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_anim_fall_down);
        mRecyclerView.setLayoutAnimation(animation);
        mCoinAdapterHome = new CoinAdapterHome(getContext());
        mRecyclerView.setAdapter(mCoinAdapterHome);
        Disposable getDataFromDB = App.getDatabase().coinInfoDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<CoinInfo>>() {
                    @Override
                    public void accept(List<CoinInfo> coinInfos) {
                        final ArrayList<CoinInfo> coinInfoList = new ArrayList<>(coinInfos);
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                mCoinAdapterHome.setCoinData(coinInfoList);
                            }
                        });
                    }
                });
        startConnectionApi();
        return view;
    }

    public void startConnectionApi() {
        Network.getInstance()
                .getApiCryptoCompare()
                .getTopListData(100, 0, "USD")
                .enqueue(new Callback<CoinCryptoCompare>() {
                    @Override
                    public void onResponse(@NonNull Call<CoinCryptoCompare> call, @NonNull Response<CoinCryptoCompare> response) {
                        mCoinCryptoCompare = response.body();
                        if (mCoinCryptoCompare != null) {
                            ArrayList<CoinInfo> coinInfoList = getCoinInfoList(mCoinCryptoCompare);
                            App.getDatabase().coinInfoDao().insert(coinInfoList);
                        }
                    }


                    @Override
                    public void onFailure(@NonNull Call<CoinCryptoCompare> call, @NonNull Throwable t) {
                        Log.e("ERROR", t.toString());
                    }
                });
    }

    private ArrayList<CoinInfo> getCoinInfoList(CoinCryptoCompare mCoinCryptoCompare) {
        ArrayList<CoinInfo> coinInfoArrayList = new ArrayList<>();
        CoinInfo coinInfo;
        for (int i = 0; i < mCoinCryptoCompare.getData().size(); i++) {
            String fullName = mCoinCryptoCompare.getData().get(i).getCoinInfo().getFullName();
            String shortName = mCoinCryptoCompare.getData().get(i).getCoinInfo().getName();
            String price = mCoinCryptoCompare.getData().get(i).getDISPLAY().getUSD().getPRICE();
            String imageURL = mCoinCryptoCompare.getData().get(i).getCoinInfo().getImageUrl();
            coinInfo = new CoinInfo(fullName, shortName, price, imageURL);
            coinInfoArrayList.add(coinInfo);
        }
        return coinInfoArrayList;
    }

}