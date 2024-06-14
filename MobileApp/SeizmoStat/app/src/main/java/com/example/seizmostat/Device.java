package com.example.seizmostat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class Device implements Parcelable {

    private LatLng latLng;
    private String deviceId;


    public Device(LatLng latLng, String deviceId) {
        this.latLng = latLng;
        this.deviceId = deviceId;
    }

    protected Device(Parcel in) {
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        deviceId = in.readString();
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public LatLng getLatLng() {
        return latLng;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(latLng, flags);
        dest.writeString(deviceId);
    }
}
