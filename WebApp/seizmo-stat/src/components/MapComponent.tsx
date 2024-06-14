import L from "leaflet";
import "leaflet/dist/leaflet.css";
import React from "react";
import ReactDOMServer from "react-dom/server";
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import { useDevices } from "../hooks/useFetchData";
import { CustomMarker } from "./CustomMarker";
import { Chart } from "./Chart";
import "./MapComponent.css";

export interface Point {
  type: string;
  geometry: {
    type: string;
    coordinates: [number, number];
  };
  properties: {
    localDateTime: string;
    localSeismicIntensity: number;
    id: string;
    timestamp: string;
  };
}

export const MapComponent: React.FC = () => {
  const position: [number, number] = [45.815, 15.9819]; // Zagreb coordinates

  const { devices } = useDevices();

  const createCustomIcon = (seismicActivity: number) => {
    return L.divIcon({
      html: ReactDOMServer.renderToString(
        <CustomMarker intensity={seismicActivity} />
      ),
      className: "",
      iconSize: [30, 42],
      popupAnchor: [-5, -32],
    });
  };

  return (
    <MapContainer
      center={position}
      zoom={13}
      style={{ height: "100vh", width: "100%" }}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      {devices &&
        devices.map((point, idx) => (
          <Marker
            key={idx}
            position={[
              point.geometry.coordinates[0],
              point.geometry.coordinates[1],
            ]}
            icon={createCustomIcon(point.properties.localSeismicIntensity)}
          >
            <Popup>
              <h1>{point.properties.id}</h1>
              <span>
                Latest reading: {point.properties.localSeismicIntensity} at{" "}
                {point.properties.timestamp}
              </span>
              <Chart id={point.properties.id} width={800} height={400} />
            </Popup>
          </Marker>
        ))}
    </MapContainer>
  );
};
