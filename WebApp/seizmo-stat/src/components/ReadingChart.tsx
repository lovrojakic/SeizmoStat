import {
  CategoryScale,
  Chart as ChartJS,
  ChartOptions,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  TimeScale,
  TimeSeriesScale,
  Title,
  Tooltip,
} from "chart.js";
import "chartjs-adapter-date-fns";
import zoomPlugin from "chartjs-plugin-zoom";
import React from "react";
import { Line } from "react-chartjs-2";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
  TimeSeriesScale,
  zoomPlugin
);

interface Reading {
  value: number;
  timestamp: Date;
}

interface ReadingChartProps {
  readings: Reading[];
  width?: number;
  height?: number;
  timeUnit: "hour" | "day" | "week" | "year";
  startDate: Date;
  endDate: Date;
}

const ReadingChart: React.FC<ReadingChartProps> = ({
  readings,
  width,
  height,
  timeUnit,
  startDate,
  endDate,
}) => {
  const data = {
    labels: readings.map((reading) => reading.timestamp),
    datasets: [
      {
        label: "Device Readings",
        data: readings.map((reading) => reading.value),
        fill: false,
        backgroundColor: "rgba(75,192,192,0.4)",
        borderColor: "rgba(75,192,192,1)",
      },
    ],
  };

  const options: ChartOptions<"line"> = {
    scales: {
      x: {
        type: "time",
        time: {
          unit: timeUnit,
        },
        min: startDate.getTime(),
        max: endDate.getTime(),
        title: {
          display: true,
          text: "Time",
        },
      },
      y: {
        title: {
          display: true,
          text: "Value",
        },
        beginAtZero: true,
      },
    },
    maintainAspectRatio: false,
    plugins: {
      zoom: {
        pan: {
          enabled: true,
          mode: "x",
        },
        zoom: {
          wheel: {
            enabled: true,
          },
          pinch: {
            enabled: true,
          },
          mode: "x",
        },
      },
    },
  };

  return (
    <div style={{ width: width || "100%", height: height || "auto" }}>
      <Line data={data} options={options} />
    </div>
  );
};

export default ReadingChart;
