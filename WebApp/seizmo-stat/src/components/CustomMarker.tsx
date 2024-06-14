// src/CustomMarker.tsx
import React from "react";

interface CustomMarkerProps {
  //   text: string;
  intensity: number;
}

export const CustomMarker: React.FC<CustomMarkerProps> = ({ intensity }) => {
  let color;
  let scaleLevel;

  if (intensity < 0.0005) {
    scaleLevel = "Not felt";
    color = "white";
  } else if (intensity < 0.003) {
    scaleLevel = "Weak";
    color = "rgb(160, 230, 255)";
  } else if (intensity < 0.028) {
    scaleLevel = "Light";
    color = "rgb(128, 255, 255)";
  } else if (intensity < 0.062) {
    scaleLevel = "Moderate";
    color = "rgb(122, 255, 147)";
  } else if (intensity < 0.12) {
    scaleLevel = "Strong";
    color = "rgb(255, 255, 0)";
  } else if (intensity < 0.22) {
    scaleLevel = "Very strong";
    color = "rgb(255, 200, 0)";
  } else if (intensity < 0.4) {
    scaleLevel = "Severe";
    color = "rgb(255, 145, 0)";
  } else if (intensity < 0.7) {
    scaleLevel = "Violent";
    color = "rgb(255, 0, 0)";
  } else {
    scaleLevel = "Extreme";
    color = "rgb(128, 0, 0)";
  }

  return (
    <div
      style={{
        backgroundColor: `${color}`,
        height: "15px",
        width: "15px",
        borderRadius: "50%",
        border: "2px solid black",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      {/* <strong>{text}</strong> */}
    </div>
  );
};
