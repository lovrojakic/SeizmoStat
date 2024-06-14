package com.example.seizmostat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GraphActivity extends AppCompatActivity {
    private ArrayList<SeismicData> seismicDataArrayList = new ArrayList<>();
    private Device device;
    private TimeRange selectedTimeRange = TimeRange.LAST_HOUR;

    enum TimeRange {
        LAST_HOUR,
        LAST_12_HOURS,
        LAST_DAY,
        ALL_HISTORY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");

        TextView nameOfDevice = findViewById(R.id.nameOfDeviceGraphView);
        nameOfDevice.setText(device.getDeviceId());

        long currentTimeMillis = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        long oneHourAgoMillis = calendar.getTimeInMillis();

        String urlHourAgo = "https://djx.entlab.hr/m2m/data?res=" + device.getDeviceId()
                + "&t1=" + oneHourAgoMillis + "&t2=" + currentTimeMillis;
        fetchStats(urlHourAgo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner dropdownMenu = findViewById(R.id.time_range_spinner);
        dropdownMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedTimeRange = TimeRange.LAST_HOUR;
                        break;
                    case 1:
                        selectedTimeRange = TimeRange.LAST_12_HOURS;
                        break;
                    case 2:
                        selectedTimeRange = TimeRange.LAST_DAY;
                        break;
                    case 3:
                        selectedTimeRange = TimeRange.ALL_HISTORY;
                        break;
                }
                fetchStatsForSelectedTimeRange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (if needed)
            }
        });
    }

    private void fetchStats(String url) {
        seismicDataArrayList = new ArrayList<>();
        String credentials = Credentials.basic("intstv_seizmostat", "N1mfSG25G4uUQIvp");

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", credentials)
                .addHeader("Accept", "application/vnd.ericsson.m2m.output+json;version=1.1")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseSeismicData(responseData);
                    plotGraph();
                } else {
                    System.out.println("Request failed: " + response.code());
                }
            }
        });
    }

    private void parseSeismicData(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            System.out.println("Request was successful: " + jsonObject.toString());
            JSONArray contentNodes = jsonObject.getJSONArray("contentNodes");
            for (int i = 0; i < contentNodes.length(); i++) {
                JSONObject event = contentNodes.getJSONObject(i);
                Date localDateTime = parseTimestamp(event.getString("time"));
                Double localSeismicIntensity = event.getDouble("value");

                System.out.println(localDateTime + " " + localSeismicIntensity);
                seismicDataArrayList.add(new SeismicData(
                        localDateTime,
                        localSeismicIntensity
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Date parseTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            System.out.println(sdf.parse(timestamp));
            return sdf.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(0);
        }
    }

    private void plotGraph() {
        runOnUiThread(() -> {
            GraphView graph = findViewById(R.id.graph);
            graph.removeAllSeries();
            DataPoint[] dataPoints = new DataPoint[seismicDataArrayList.size()];

            for (int i = 0; i < seismicDataArrayList.size(); i++) {
                Date date = seismicDataArrayList.get(i).getLocalDateTime();
                double seismicIntensity = seismicDataArrayList.get(i).getLocalSeismicIntensity();
                dataPoints[i] = new DataPoint(date.getTime(), seismicIntensity);
            }

            Arrays.sort(dataPoints, Comparator.comparing(DataPoint::getX));
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graph.addSeries(series);

            graph.setTitle("Seismic Data");

            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(GraphActivity.this, new SimpleDateFormat("\ndd-MM-yyyy\nHH:mm:ss")) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX).replace(" ", "\n");
                    } else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });

            graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");

            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);

            if (dataPoints.length > 0) {
                graph.getViewport().setMinX(dataPoints[0].getX());
                graph.getViewport().setMaxX(dataPoints[dataPoints.length - 1].getX());
            }

            graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        });
    }

    private void fetchStatsForSelectedTimeRange() {
        long currentTimeMillis = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        long agoTime;
        String urlHourAgo;

        switch (selectedTimeRange) {
            case LAST_HOUR:
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                agoTime = calendar.getTimeInMillis();
                urlHourAgo = "https://djx.entlab.hr/m2m/data?res=" + device.getDeviceId()
                        + "&t1=" + agoTime + "&t2=" + currentTimeMillis;
                fetchStats(urlHourAgo);
                break;
            case LAST_12_HOURS:
                calendar.add(Calendar.HOUR_OF_DAY, -12);
                agoTime = calendar.getTimeInMillis();
                urlHourAgo = "https://djx.entlab.hr/m2m/data?res=" + device.getDeviceId()
                        + "&t1=" + agoTime + "&t2=" + currentTimeMillis;
                fetchStats(urlHourAgo);
                break;
            case LAST_DAY:
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                agoTime = calendar.getTimeInMillis();
                urlHourAgo = "https://djx.entlab.hr/m2m/data?res=" + device.getDeviceId()
                        + "&t1=" + agoTime + "&t2=" + currentTimeMillis;
                fetchStats(urlHourAgo);
                break;
            case ALL_HISTORY:
                urlHourAgo = "https://djx.entlab.hr/m2m/data?res=" + device.getDeviceId();
                fetchStats(urlHourAgo);
                break;
        }
    }
}
