import { useEffect, useState } from "react";

export interface Reading {
  value: number;
  timestamp: Date;
}

export const useReadings = (deviceId: string, startDate: Date) => {
  const [readings, setReadings] = useState<Reading[]>([]);

  useEffect(() => {
    const fetchReadings = async () => {
      try {
        const readingsData = await getAllDeviceReadings(deviceId);
        setReadings(readingsData);
      } catch (err: any) {}
    };

    fetchReadings();
  }, [deviceId]);

  return { readings };
};

const getAllDeviceReadings = async (deviceId: string) => {
  try {
    const response = await fetch(`/m2m/data?res=${deviceId}`, {
      method: "GET",
      headers: {
        Authorization: "Basic " + btoa("intstv_seizmostat:N1mfSG25G4uUQIvp"),
        Accept: "application/vnd.ericsson.m2m.output+json;version=1.1",
      },
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.statusText}`);
    }

    const result = await response.json();

    const readings = result.contentNodes.map((reading: any) => {
      return {
        value: reading.value,
        timestamp: new Date(reading.time),
      };
    });

    return readings;
  } catch (err: any) {
    throw err;
  }
};
