import { FC } from "react";
import "./Legend.css";

export const Legend: FC<{ isDisplayed?: boolean }> = ({ isDisplayed }) => {
  return (
    <>
      <div
        className="legend-container"
        style={{ display: isDisplayed ? "" : "none" }}
      >
        <h1>Legend:</h1>
        <div className="row">
          <span>{"< 0.0005 - Not felt"} </span>
          <div
            className="marker"
            style={{
              backgroundColor: "white",
              marginTop: 0,
            }}
          />
        </div>
        <div className="row">
          <p>{"< 0.003 - Weak"}</p>
          <div
            style={{
              backgroundColor: "rgb(160, 230, 255)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.028 - Light"}</p>
          <div
            style={{
              backgroundColor: "rgb(128, 255, 255)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.062 - Moderate"}</p>
          <div
            style={{
              backgroundColor: "rgb(122, 255, 147)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.12 - Strong"}</p>
          <div
            style={{
              backgroundColor: "rgb(255, 255, 0)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.22 - Very strong"}</p>
          <div
            style={{
              backgroundColor: "rgb(255, 200, 0)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.4 - Severe"}</p>
          <div
            style={{
              backgroundColor: "rgb(255, 145, 0)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{"< 0.7 - Violent"}</p>
          <div
            style={{
              backgroundColor: "rgb(255, 0, 0)",
            }}
            className="marker"
          />
        </div>
        <div className="row">
          <p>{">= 0.7 - Extreme"}</p>
          <div
            style={{
              backgroundColor: "rgb(128, 0, 0)",
            }}
            className="marker"
          />
        </div>
      </div>
    </>
  );
};
