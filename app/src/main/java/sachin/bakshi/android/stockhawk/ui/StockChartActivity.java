package sachin.bakshi.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sachin.bakshi.android.stockhawk.R;

public class StockChartActivity extends AppCompatActivity {

    List<PointValue> yValues = new ArrayList<PointValue>();
    List<AxisValue> axisValues = new ArrayList<AxisValue>();
    LineChartView chart;
    ProgressBar mProgressBar;
    private Context mContext;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        mContext = this;
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);
        chart = (LineChartView) findViewById(R.id.chart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationContentDescription("Go Back ");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Intent symbol = getIntent();
        String sym = symbol.getStringExtra("Symbol");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String sixMonthBackDate = format.format(date);
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol =\""
                    + sym + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("and startDate =\"" + sixMonthBackDate + "\"" + "and endDate =\"" + todayDate + "\"", "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            doGetRequest(urlStringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(mContext, R.string.network_request_failed, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        String res = response.body().string();
                        response.body().close();

                        try {
                            yValues.clear();
                            axisValues.clear();
                            JSONObject reader = new JSONObject(res);
                            JSONObject query = reader.getJSONObject("query");
                            JSONObject results = query.getJSONObject("results");
                            JSONArray quotes = results.getJSONArray("quote");
                            for (int i = 0; i < quotes.length(); i++) {
                                JSONObject c = quotes.getJSONObject(i);
                                String date = c.getString("Date");
                                String price = c.getString("Open");
                                yValues.add(new PointValue(i * 100, Float.valueOf(price)));
                                AxisValue axisValue = new AxisValue(i * 100);
                                axisValue.setLabel(date);
                                axisValues.add(axisValue);


                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mProgressBar.setVisibility(View.GONE);
                                    chart.setVisibility(View.VISIBLE);
                                    setChartData();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
    }

    public void setChartData() {


        Line line = new Line(yValues).setColor(Color.YELLOW).setCubic(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(new Axis(axisValues));
        Axis axisX = new Axis().setHasLines(false);
        Axis axisY = new Axis().setHasLines(false);
        axisX.setName("Dates");
        axisY.setName("Prices");
        data.setAxisYLeft(axisY);
        chart.setLineChartData(data);
    }
}
