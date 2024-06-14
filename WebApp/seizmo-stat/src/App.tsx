import { useState } from "react";
import "./App.css";
import { Legend } from "./components/Legend";
import { MapComponent } from "./components/MapComponent";

function App() {
  const [displayLegend, setDisplayLegend] = useState<boolean>(true);

  return (
    <div className="App">
      <MapComponent />
      <Legend isDisplayed={displayLegend} />
      <button
        style={{
          position: "fixed",
          top: 0,
          right: 0,
          zIndex: 99999,
          margin: 10,
        }}
        onClick={() => {
          setDisplayLegend(!displayLegend);
        }}
      >
        Toggle legend
      </button>
    </div>
  );
}

export default App;
