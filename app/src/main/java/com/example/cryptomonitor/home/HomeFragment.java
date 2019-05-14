package com.example.cryptomonitor.home;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.ToolbarInteractor;
import com.example.cryptomonitor.activity.MainActivity;
import com.example.cryptomonitor.database.entities.CoinInfo;
import com.example.cryptomonitor.events.Event;
import com.example.cryptomonitor.events.Message;
import com.jakewharton.rxbinding2.widget.RxAdapterView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment implements CoinAdapterHome.OnStarClickListener,
        ToolbarInteractor,
        SwipeRefreshLayout.OnRefreshListener,
        CoinAdapterHome.OnCoinClickListener {

    public static final String TAG = "MyLogs";
    private String mCurrency;
    private HomeViewModel mHomeViewModel;
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecyclerView;
    private CoinAdapterHome mCoinAdapterHome;
    private EditText mSearch;
    private ImageView mSearchOn;
    private ImageView mSearchOff;
    private Spinner mSpinner;
    private LinearLayout mLinearSpinner;
    private LinearLayout mLinearSearch;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable disposableSearch;
    private Disposable disposableSpinner;
    private String[] spinnerArray;
    private SharedPreferences sharedPreferences;
    private String spinnerPosition = "spinner_position";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);



        mRecyclerView = view.findViewById(R.id.rv_coin_itemlist);
        mSwipeRefresh = view.findViewById(R.id.swipe_refresh);

        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(R.color.dark1, R.color.dark2, R.color.dark3);

        mCoinAdapterHome = new CoinAdapterHome(getContext());
        mCoinAdapterHome.setup(this);

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_anim_fall_down);
        mRecyclerView.setLayoutAnimation(animation);
        mRecyclerView.setAdapter(mCoinAdapterHome);

        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mHomeViewModel.getSearchModeLiveData().observe(this, listObserver);
        mHomeViewModel.getEventLiveData().observe(this, eventObserver);
        mHomeViewModel.getSwipeRefreshLiveData().observe(this, swipeRefreshObserver);

        mSearch = view.findViewById(R.id.home_search);
        mSearchOn = view.findViewById(R.id.home_search_on);
        mSearchOff = view.findViewById(R.id.home_search_off);
        mSpinner = view.findViewById(R.id.home_spinner);
        mLinearSpinner = view.findViewById(R.id.home_linear_spinner);
        mLinearSearch = view.findViewById(R.id.home_linear_search);

        loadPosition();

        mSearchOn.setOnClickListener(v -> {
            mLinearSpinner.setVisibility(View.GONE);
            mLinearSearch.setVisibility(View.VISIBLE);
            mSearch.requestFocus();
        });
        mSearchOff.setOnClickListener(v -> {
            mLinearSpinner.setVisibility(View.VISIBLE);
            mLinearSearch.setVisibility(View.GONE);
            mSearch.getText().clear();
            mHomeViewModel.onSearchDeactivated();
        });


        disposableSearch = RxTextView
                .textChangeEvents(mSearch)
                .skipInitialValue()
                .map(TextViewTextChangeEvent::text)
                .map(CharSequence::toString)
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(s -> !s.isEmpty())
                .doOnNext(s -> Log.e("Check stream", s))
                .subscribe(s -> mHomeViewModel.onTextChanged(s));


        disposableSpinner = RxAdapterView
                .itemSelections(mSpinner)
                .skipInitialValue()
                .skip(2)
                .subscribe(position -> {
                    String currency = spinnerArray[position];
                    mHomeViewModel.refresh(currency);
                });

        return view;
    }

    @Override
    public void onStarClick(CoinInfo clickedCoinInfo) {
        mHomeViewModel.onStarClicked(clickedCoinInfo);
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String currentText) {
        mHomeViewModel.onTextChanged(currentText);
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                mHomeViewModel.onSearchClicked();
                break;
        }
    }

    @Override
    public boolean onClose() {
        mHomeViewModel.onSearchDeactivated();
        return false;
    }

    @Override
    public void onDestroy() {
        savePosition();
        if (!disposableSearch.isDisposed()) disposableSearch.dispose();
        if (!disposableSpinner.isDisposed()) disposableSpinner.dispose();
        mHomeViewModel = null;
        mCoinAdapterHome = null;
        mRecyclerView = null;
        listObserver = null;
        mSwipeRefresh = null;
        super.onDestroy();
    }

    private Observer<List<CoinInfo>> listObserver = coinInfoList -> {
        if (coinInfoList != null)
            mCoinAdapterHome.setList(coinInfoList);
        else
            mCoinAdapterHome.showMode();
    };

    private Observer<Event> eventObserver = event -> {
        if (event != null && !event.isHandled()) {
            if (event instanceof Message) {
                Message message = (Message) event;
                Toast.makeText(getContext(), message.getMessageText(), Toast.LENGTH_SHORT).show();
                message.handled();
            }
        }
    };

    private Observer<Boolean> swipeRefreshObserver = isRefreshing -> {
        if (isRefreshing) {
            mSwipeRefresh.setRefreshing(true);
            mCoinAdapterHome.setRefreshing(true);
        } else {
            mSwipeRefresh.setRefreshing(false);
            mCoinAdapterHome.setRefreshing(false);
        }
    };


    @Override
    public void setCurrency(String currency) {
        mCurrency = currency;
        onRefresh();
    }

    @Override
    public void onRefresh() {
       /* if (mHomeViewModel != null)
            mHomeViewModel.refresh(mCurrency);*/
    }

    @Override
    public void onCoinClick(String index, int position) {
        ((MainActivity) getActivity()).onCoinClicked(index, position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        spinnerArray = context.getResources().getStringArray(R.array.spinner);
    }

    void savePosition() {
        sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(spinnerPosition, mSpinner.getSelectedItemPosition());
        editor.commit();
    }

    void loadPosition() {
        sharedPreferences = getActivity().getPreferences(MODE_PRIVATE);
        int position = sharedPreferences.getInt(spinnerPosition, 0);
        mSpinner.setSelection(position);
    }
}
