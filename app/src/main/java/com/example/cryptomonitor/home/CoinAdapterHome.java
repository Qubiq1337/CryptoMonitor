package com.example.cryptomonitor.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptomonitor.R;
import com.example.cryptomonitor.database.App;
import com.example.cryptomonitor.database.coins.CoinInfo;
import com.example.cryptomonitor.database.coins.CoinInfoDao;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class CoinAdapterHome extends RecyclerView.Adapter<CoinAdapterHome.CoinViewHolder> {

    private final static int initialSize = 60;
    private final static int loadSize = 20;
    private Context mContext;
    private List<CoinInfo> mData;
    private OnStarClickListener mOnStarClickListener;
    private OnCoinClickListener mOnCoinClickListener;
    private boolean isLoading = false;
    private boolean showMode = false;
    private CoinInfoDao mDao;
    private Disposable disposable;

    private Consumer<List<CoinInfo>> mListConsumer = coinInfoList -> {
        mData = coinInfoList;
        notifyDataSetChanged();
        isLoading = false;
    };

    CoinAdapterHome(Context context) {
        this.mContext = context;
        mData = new ArrayList<>();
    }


    void setup(Fragment fragment) {
        this.mOnStarClickListener = (OnStarClickListener) fragment;
        this.mOnCoinClickListener = (OnCoinClickListener) fragment;
        mDao = App.getDatabase().coinInfoDao();
        showMode();
    }

    void showMode() {
        showMode = true;
        if (disposable != null)
            disposable.dispose();
        disposable = mDao.getAllBefore(initialSize)
                .subscribeOn(Schedulers.io())
                .subscribe(this::toSortedList);
    }

    private void toSortedList(List<CoinInfo> coinInfoList) {
        Disposable sortedDisposable = Flowable.fromIterable(coinInfoList)
                .subscribeOn(Schedulers.computation())
                .toSortedList((coinInfo1, coinInfo2) -> Double.compare(coinInfo2.getMktcap_double(), coinInfo1.getMktcap_double()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListConsumer);
    }

    void setList(List<CoinInfo> coinInfoList) {
        showMode = false;
        if (disposable != null)
            disposable.dispose();
        mData = coinInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_coin_layout, viewGroup, false);
        return new CoinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder coinViewHolder, int index) {
        if (index + loadSize > mData.size() && !isLoading && showMode) {
            loadMore();
        }
        CoinInfo coin = mData.get(index);
        coinViewHolder.textViewFullName.setText(coin.getFullName());
        coinViewHolder.textViewName.setText(coin.getShortName());
        coinViewHolder.textViewPrice.setText(coin.getPriceDisplay());
        Picasso.get()
                .load(coin.getImageURL())
                .transform(new PicassoCircleTransformation())
                .into(coinViewHolder.imageViewIcon);
        if (coin.isFavorite())
            coinViewHolder.isFavoriteImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_star));
        else
            coinViewHolder.isFavoriteImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_star_outline));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void loadMore() {
        isLoading = true;
        if (disposable != null)
            disposable.dispose();
        disposable = mDao.getAllBefore(getItemCount() + loadSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mListConsumer);
    }

    public interface OnStarClickListener {
        void onStarClick(CoinInfo coinInfo);
    }

    public interface OnCoinClickListener {
        void onCoinClick(String index, int position);
    }

    public static class PicassoCircleTransformation implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    class CoinViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewPrice;
        private TextView textViewFullName;
        private TextView textViewName;
        private ImageView imageViewIcon;
        private ImageView isFavoriteImage;

        CoinViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFullName = itemView.findViewById(R.id.rv_coin_layout_fullname);
            textViewName = itemView.findViewById(R.id.rv_coin_layout_name);
            textViewPrice = itemView.findViewById(R.id.rv_coin_layout_price);
            imageViewIcon = itemView.findViewById(R.id.rv_coin_layout_icon);
            isFavoriteImage = itemView.findViewById(R.id.rv_coin_favorite_image);
            isFavoriteImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position >= 0) {
                    CoinInfo clickedCoin = mData.get(position);
                    if (clickedCoin.isFavorite())
                        clickedCoin.setFavorite(false);
                    else
                        clickedCoin.setFavorite(true);
                    notifyItemChanged(position);
                    mOnStarClickListener.onStarClick(clickedCoin);
                }
            });
            itemView.setOnClickListener(v ->
                    mOnCoinClickListener.onCoinClick(mData.get(getAdapterPosition()).getShortName(), getAdapterPosition()));
        }

    }
}
