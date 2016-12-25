package com.sam_chordas.android.stockhawk;

/**
 * Created by kapil pc on 12/17/2016.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class WidgetDataProvider implements RemoteViewsFactory {

    List mSymbol = new ArrayList<String>();
    List mPrice = new ArrayList<String>();
    List mChange = new ArrayList<String>();
    List mIsUp = new ArrayList<>();

    Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mSymbol.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_quote);
        mView.setTextViewText(R.id.stock_symbol, mSymbol.get(position).toString());
        mView.setTextViewText(R.id.bid_price, mPrice.get(position).toString());
        mView.setTextViewText(R.id.change, mChange.get(position).toString());
        if (mIsUp.get(position).toString() == Integer.toString(1)){
            mView.setInt(R.id.change,"setBackgroundResource", R.drawable.percent_change_pill_green);
        } else{
            mView.setInt(R.id.change,"setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        mSymbol.clear();
        mPrice.clear();
        mChange.clear();
        mIsUp.clear();
        Cursor c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[] { QuoteColumns.SYMBOL,QuoteColumns.BIDPRICE,QuoteColumns.CHANGE,QuoteColumns.ISUP }, null,
                null, null);
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            mSymbol.add(c.getString(c.getColumnIndex(QuoteColumns.SYMBOL)));
            mPrice.add(c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE)));
            mChange.add(c.getString(c.getColumnIndex(QuoteColumns.CHANGE)));
            mIsUp.add(c.getInt(c.getColumnIndex(QuoteColumns.ISUP)));


        }
        c.close();
    }

    @Override
    public void onDestroy() {

    }

}

