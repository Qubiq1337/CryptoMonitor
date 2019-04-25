package com.example.cryptomonitor.adapters;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.entities.CoinInfo;
import com.squareup.picasso.Picasso;

public class MinCoinAdapter extends PagedListAdapter<CoinInfo, MinCoinAdapter.MinCoinViewHolder> {

    private static final String ICONS_MASTER_32_X_32 = "https://raw.githubusercontent.com/MoneyConverter/cryptocurrencies-icons/master/32x32/";
    private Context mContext;
    private OnItemClickListener mOnClickListener;

    public interface OnItemClickListener {
        void OnItemClick(CoinInfo coinInfo);
    }

    public MinCoinAdapter(DiffUtil.ItemCallback<CoinInfo> diffCallback, Context context, OnItemClickListener onItemClickListener) {
        super(diffCallback);
        this.mContext = context;
        this.mOnClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MinCoinViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.min_coin_item, viewGroup, false);
        return new MinCoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MinCoinViewHolder minCoinViewHolder, int i) {
        final CoinInfo coinInfo = getCurrentList().get(i);
        String URL = ICONS_MASTER_32_X_32 + getCurrentList().get(i).getShortName().toLowerCase() + ".png";
        Picasso.with(mContext).load(URL).into(minCoinViewHolder.mIconImage);
        minCoinViewHolder.mCoinNameTv.setText(coinInfo.getFullName());
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }


    class MinCoinViewHolder extends RecyclerView.ViewHolder {
        private TextView mCoinNameTv;
        private ImageView mIconImage;

        MinCoinViewHolder(@NonNull View itemView) {
            super(itemView);
            mCoinNameTv = itemView.findViewById(R.id.rv_item_name);
            mIconImage = itemView.findViewById(R.id.item_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.OnItemClick(getCurrentList().get(getAdapterPosition()));
                }
            });
        }
    }
}
