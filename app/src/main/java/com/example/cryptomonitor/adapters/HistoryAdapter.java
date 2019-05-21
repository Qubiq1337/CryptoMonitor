package com.example.cryptomonitor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.entities.Bill;
import com.example.cryptomonitor.database.entities.Purchase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.cryptomonitor.Utilities.cashFormatting;
import static com.example.cryptomonitor.Utilities.simpleNumberFormatting;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Bill> mBillList;
    private Context mContext;

    public HistoryAdapter(Context context){
        mContext = context;
        mBillList = new ArrayList<>();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.history_item,parent,false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Bill bill = mBillList.get(position);
        holder.name.setText(bill.getFull_name());
        holder.sellDate.setText(bill.getSell_date());
        holder.buyDate.setText(bill.getBuy_date());
        holder.sellPrice.setText(cashFormatting(bill.getSellPrice()));
        holder.buyPrice.setText(cashFormatting(bill.getBuyPrice()));
        String amountStr = cashFormatting(bill.getAmount()) + " " + bill.getShort_name();
        holder.amount.setText(amountStr);
        Picasso.with(mContext).load(bill.getImage_url()).into(holder.icon);

    }

    @Override
    public int getItemCount() {
        return mBillList.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView buyPrice;
        private TextView sellPrice;
        private TextView buyDate;
        private TextView sellDate;
        private TextView amount;
        private TextView name;
        private ImageView icon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            buyPrice = itemView.findViewById(R.id.history_buy_price);
            sellPrice = itemView.findViewById(R.id.history_sell_price);
            buyDate = itemView.findViewById(R.id.history_buy_date);
            sellDate = itemView.findViewById(R.id.history_sell_date);
            amount = itemView.findViewById(R.id.history_amount);
            name = itemView.findViewById(R.id.history_item_name);
            icon = itemView.findViewById(R.id.history_item_icon);
        }
    }

    public void setBillList(List<Bill> mBillList) {
        this.mBillList = mBillList;
        notifyDataSetChanged();
    }
}