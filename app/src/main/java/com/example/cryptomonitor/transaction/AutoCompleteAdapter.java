package com.example.cryptomonitor.transaction;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.App;
import com.example.cryptomonitor.database.coins.CoinInfo;
import com.example.cryptomonitor.home.CoinAdapterHome;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AutoCompleteAdapter extends BaseAdapter implements Filterable {
    private List<CoinInfo> coinInfoList;
    private List<CoinInfo> resultFilterList;
    private Context mContext;

    AutoCompleteAdapter(Context context) {
        mContext = context;
        resultFilterList = new ArrayList<>();
        coinInfoList = new ArrayList<>();
        Disposable disposable = App
                .getDatabase()
                .coinInfoDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    coinInfoList = list;
                    Log.e("findCoin", coinInfoList.size() + "");
                });
    }

    List<CoinInfo> getResultFilterList() {
        return resultFilterList;
    }

    @Override
    public int getCount() {
        return resultFilterList.size();
    }

    @Override
    public CoinInfo getItem(int index) {
        return resultFilterList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.autocomplete_tv_item, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.autocomplete_item_full_name);
        ImageView imageView = convertView.findViewById(R.id.autocomplete_item_icon);
        CoinInfo coinInfo = getItem(position);

        if (coinInfo != null) {
            textView.setText(coinInfo.getFullName());
            Picasso.get()
                    .load(coinInfo.getImageURL())
                    .transform(new CoinAdapterHome.PicassoCircleTransformation())
                    .into(imageView);
        }

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                List<CoinInfo> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    suggestions.addAll(coinInfoList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (CoinInfo item : coinInfoList) {
                        if (item.getFullName().toLowerCase().contains(filterPattern)) {
                            suggestions.add(item);
                        }
                    }
                }
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultFilterList = (List<CoinInfo>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((CoinInfo) resultValue).getFullName();
            }

        };
    }
}
