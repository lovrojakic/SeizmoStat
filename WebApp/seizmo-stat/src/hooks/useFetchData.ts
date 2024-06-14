import { useState, useEffect } from "react";
import { Point } from "../components/MapComponent";
import { Reading } from "./useReadings";
import { isEqual } from "lodash";

export const useDevices = () => {
  const [devices, setDevices] = useState<Point[] | undefined>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isSubscribed = true;

    const fetchDevices = async () => {
      try {
        const devicesData = await getDevices();
        if (isSubscribed && !isEqual(devices, devicesData)) {
          setDevices(devicesData);
        }
      } catch (err: any) {
        if (isSubscribed) {
          setError(err.message);
        }
      } finally {
        if (isSubscribed) {
          setLoading(false);
        }
      }
    };

    fetchDevices();
    const intervalId = setInterval(fetchDevices, 10000);

    return () => {
      isSubscribed = false;
      clearInterval(intervalId);
    };
  }, [devices]); // Add devices to the dependency array to ensure it checks for changes

  return { devices, loading, error };
};

const getDevices = async () => {
  try {
    const response = await fetch(
      "/m2m/provisioning/sensor/SiezmoStatAccelerometer/resource",
      {
        method: "GET",
        headers: {
          Authorization: "Basic " + btoa("intstv_seizmostat:N1mfSG25G4uUQIvp"),
          Accept: "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const result = await response.json();

    const devicesWithLocation: Array<Point> = await Promise.all(
      result.map(async (device: any) => {
        const location = await getDeviceLocation(device.urn);
        const reading = await getLatestReading(device.urn);

        return {
          type: "Feature",
          geometry: {
            type: "Point",
            coordinates: location,
          },
          properties: {
            localDateTime: "2024-06-03T12:30:00",
            localSeismicIntensity: reading?.value || 0,
            id: device.urn,
            timestamp: reading?.timestamp || "2024-06-03T12:30:00",
          },
        };
      })
    );

    return devicesWithLocation;
  } catch (err: any) {}
};

const getDeviceLocation = async (deviceId: string) => {
  try {
    const response = await fetch(
      `/m2m/provisioning/resource/${deviceId}/attribute`,
      {
        method: "GET",
        headers: {
          Authorization: "Basic " + btoa("intstv_seizmostat:N1mfSG25G4uUQIvp"),
          Accept: "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const result = await response.json();

    return [result[0].value, result[1].value];
  } catch (err: any) {}
};

const getLatestReading = async (deviceId: string) => {
  try {
    const response = await fetch(
      `/m2m/data?res=${deviceId}&maxResourcesPerPage=1`,
      {
        method: "GET",
        headers: {
          Authorization: "Basic " + btoa("intstv_seizmostat:N1mfSG25G4uUQIvp"),
          Accept: "application/vnd.ericsson.m2m.output+json;version=1.1",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const result = await response.json();

    return result.contentNodes[0] as Reading;
  } catch (err: any) {}
};
