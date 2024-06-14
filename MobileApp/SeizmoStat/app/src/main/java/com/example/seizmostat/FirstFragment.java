package com.example.seizmostat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class FirstFragment extends Fragment {
    private LinearLayout deviceContainer;
    private ArrayList<Device> devices = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_first, container, false);

        deviceContainer = view.findViewById(R.id.device_container);

//        String localhost = "172.19.208.1";
//        createDevices("http://" + localhost + ":8080/test/devices");

        String sernsor = "SiezmoStatAccelerometer";
        getDevices("https://djx.entlab.hr/m2m/provisioning/sensor/" + sernsor + "/resource");

        Button openMaps = view.findViewById(R.id.buttonYourLocation);
        openMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putParcelableArrayListExtra("devices", devices);
                startActivity(intent);
            }
        });

        return view;
    }

    private void getDevices(String url) {
        String credentials = Credentials.basic("intstv_seizmostat", "N1mfSG25G4uUQIvp");

        // Build the request with headers
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", credentials)
                .addHeader("Accept", "application/json")
                .build();

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println(responseData);
                    createDevices(responseData, client, credentials);
                } else {
                    System.out.println("Request failed: " + response.code());
                }
            }
        });
    }

    private void createDevices(String jsonResponse, OkHttpClient client, String credentials) {
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            List<JSONObject> jsonList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonList.add(jsonArray.getJSONObject(i));
            }

            Collections.sort(jsonList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String urnA = null;
                    String urnB = null;
                    try {
                        urnA = a.getString("urn");
                        urnB = b.getString("urn");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return urnA.compareTo(urnB);
                }
            });
            JSONArray sortedJsonArray = new JSONArray(jsonList);

            for (int i = 0; i < sortedJsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String deviceId = object.getString("urn");

                String url = "https://djx.entlab.hr/m2m/provisioning/resource/"+deviceId+"/attribute";
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", credentials)
                        .addHeader("Accept", "application/json")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            System.out.println(responseData);

                            try {
                                JSONArray jsonArrayCoordinates = new JSONArray(responseData);

                                LatLng latLng = new LatLng(
                                        jsonArrayCoordinates.getJSONObject(0).getDouble("value"),
                                        jsonArrayCoordinates.getJSONObject(1).getDouble("value"));

                                Device device = new Device(
                                        latLng,
                                        deviceId
                                );
                                getActivity().runOnUiThread(() -> addDeviceView(device));
                                devices.add(device);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            System.out.println("Request failed: " + response.code());
                        }
                    }
                });

                for (Device device : devices) {
                    addDeviceView(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDeviceView(Device device) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View deviceView = inflater.inflate(R.layout.device_layout, deviceContainer, false);

        TextView deviceName = deviceView.findViewById(R.id.device_name);
        Button buttonToMaps = deviceView.findViewById(R.id.button_to_maps);
        Button buttonShowGraph = deviceView.findViewById(R.id.button_show_graph);

        deviceName.setText(device.getDeviceId());
        buttonToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("devices", devices);
                intent.putExtra("device", device);
                startActivity(intent);
            }
        });

        buttonShowGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GraphActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            }
        });

        deviceContainer.addView(deviceView);
    }

}


