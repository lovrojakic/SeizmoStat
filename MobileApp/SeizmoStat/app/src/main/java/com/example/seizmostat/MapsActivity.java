package com.example.seizmostat;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.seizmostat.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<Device> devices;
    private Device device;
    private HashMap<Marker, Device> markerDeviceMap = new HashMap<>();
    private Marker lastClickedMarker;
    private Device lastClickedDevice = null;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final int FINE_PERMISSION_CODE = 1;
    private Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        devices = intent.getParcelableArrayListExtra("devices");
        device = intent.getParcelableExtra("device");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        String credentials = Credentials.basic("intstv_seizmostat", "N1mfSG25G4uUQIvp");
        OkHttpClient client = new OkHttpClient();

        for (Device device1 : devices) {
            String url = "https://djx.entlab.hr/m2m/data?res=" + device1.getDeviceId();
            System.out.println(url);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", credentials)
                    .addHeader("Accept", "application/vnd.ericsson.m2m.output+json;version=1.1")
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
                        try {
                            System.out.println(device1.getDeviceId() + " response data: " + responseData);
                            JSONObject jsonObject = new JSONObject(responseData);
                            float color = BitmapDescriptorFactory.HUE_AZURE;
                            if (jsonObject.length() != 0) {
                                double measure = jsonObject
                                        .getJSONArray("contentNodes")
                                        .getJSONObject(0)
                                        .getDouble("value");

                                if (measure < 0.0005) {
                                    color = BitmapDescriptorFactory.HUE_CYAN;
                                } else if (measure < 0.003) {
                                    color = BitmapDescriptorFactory.HUE_CYAN;
                                } else if (measure < 0.028) {
                                    color = BitmapDescriptorFactory.HUE_ROSE;
                                } else if (measure < 0.062) {
                                    color = BitmapDescriptorFactory.HUE_MAGENTA;
                                } else if (measure < 0.12) {
                                    color = BitmapDescriptorFactory.HUE_VIOLET;
                                } else if (measure < 0.22) {
                                    color = BitmapDescriptorFactory.HUE_GREEN;
                                } else if (measure < 0.40) {
                                    color = BitmapDescriptorFactory.HUE_YELLOW;
                                } else if (measure < 0.75) {
                                    color = BitmapDescriptorFactory.HUE_ORANGE;
                                } else {
                                    color = BitmapDescriptorFactory.HUE_RED;
                                }
                            }

                            MarkerOptions markerOption = new MarkerOptions()
                                    .position(device1.getLatLng())
                                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                                    .title(device1.getDeviceId());

                            runOnUiThread(() -> {
                                Marker marker = mMap.addMarker(markerOption);
                                if (marker != null) {
                                    markerDeviceMap.put(marker, device1);
                                }
                            });
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        System.out.println("Request failed: " + response.code());
                    }
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        getLastLocation();
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(v -> moveCameraToUserLocation());
    }

    private void moveCameraToUserLocation() {
        if (currentLocation != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLatLng, 15);
            mMap.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("My location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                if (userMarker != null) {
                    userMarker.showInfoWindow(); // Show the info window immediately
                }

                if (device != null) {
                    CameraUpdate zoomToDevice = CameraUpdateFactory.newLatLngZoom(device.getLatLng(), 15);
                    mMap.animateCamera(zoomToDevice);
                } else {
                    CameraUpdate zoomToUser = CameraUpdateFactory.newLatLngZoom(userLatLng, 15);
                    mMap.animateCamera(zoomToUser);
                }

                startLocationUpdates();
            }
        });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                currentLocation = location;
                LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if (userMarker != null) {
                    userMarker.setPosition(userLatLng);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the device associated with the clicked marker
        Device clickedDevice = markerDeviceMap.get(marker);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (clickedDevice != null) {
                if (lastClickedDevice == null) {
                    lastClickedDevice = clickedDevice;
                } else if (lastClickedDevice.equals(clickedDevice)) {
                    showGraphDialog(clickedDevice);
                } else {
                    lastClickedDevice = clickedDevice;
                }
            }
        }, 800);

        return false; // Return false to indicate that the default behavior should occur (e.g., the info window should be shown)
    }

    private void showGraphDialog(Device clickedDevice) {
        new AlertDialog.Builder(this)
                .setTitle("View Graph")
                .setMessage("Do you want to see the graph for " + clickedDevice.getDeviceId() + " device?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(MapsActivity.this, GraphActivity.class);
                    intent.putExtra("device", clickedDevice);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }
}
