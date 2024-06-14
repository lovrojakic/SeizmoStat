// src/components/Chart.tsx
import React, { FC, useState } from "react";
import ReadingChart from "./ReadingChart";
import { useReadings } from "../hooks/useReadings";
import { aggregateReadings } from "../utils/aggregateReadings";
import { getTimeRange } from "../utils/getTimeRange";

interface ChartProps {
  id: string;
  width?: number;
  height?: number;
}

export const Chart: FC<ChartProps> = ({ id, width, height }) => {
  const [timeRange, setTimeRange] = useState<string>("today");
  const [startDate, endDate, timeUnit] = getTimeRange(timeRange);
  const { readings } = useReadings(id, startDate);

  const filteredReadings = readings.filter(
    (reading) => reading.timestamp >= startDate && reading.timestamp <= endDate
  );

  const ar = aggregateReadings(filteredReadings, timeUnit, startDate, endDate);

  return (
    <div>
      <label htmlFor="timeRange">Time Range: </label>
      <select
        id="timeRange"
        value={timeRange}
        onChange={(e) => setTimeRange(e.target.value)}
      >
        <option value="today">Today</option>
        <option value="lastWeek">Last Week</option>
        <option value="lastMonth">Last Month</option>
        <option value="all">All Time</option>
      </select>
      {filteredReadings && (
        <ReadingChart
          readings={ar}
          width={width}
          height={height}
          timeUnit={timeUnit}
          startDate={startDate}
          endDate={endDate}
        />
      )}
    </div>
  );
};
