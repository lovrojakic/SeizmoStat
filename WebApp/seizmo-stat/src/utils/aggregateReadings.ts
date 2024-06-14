import {
  startOfHour,
  startOfDay,
  startOfWeek,
  startOfMonth,
  getTime,
  addHours,
  addDays,
  addWeeks,
  addYears,
  isBefore,
  isEqual,
} from "date-fns";

interface Reading {
  value: number;
  timestamp: Date;
}

const aggregateReadings = (
  readings: Reading[],
  timeUnit: "hour" | "day" | "week" | "year",
  startDate: Date,
  endDate: Date
): Reading[] => {
  if (!readings || readings.length === 0) return [];

  // Sort readings by timestamp
  readings.sort((a, b) => a.timestamp.getTime() - b.timestamp.getTime());

  // Increment function based on the time unit
  const incrementDate = (date: Date): Date =>
    new Date(
      timeUnit === "hour"
        ? addHours(date, 1)
        : timeUnit === "day"
        ? addDays(date, 1)
        : timeUnit === "week"
        ? addWeeks(date, 1)
        : addYears(date, 1)
    );

  const aggregatedReadings: Reading[] = [];
  let currentDate = startDate;

  while (isBefore(currentDate, endDate) || isEqual(currentDate, endDate)) {
    const timeKey = getTime(currentDate);

    let hasReadings = false;

    for (const reading of readings) {
      let readingTimeKey: number;
      switch (timeUnit) {
        case "hour":
          readingTimeKey = getTime(startOfHour(reading.timestamp));
          break;
        case "day":
          readingTimeKey = getTime(startOfDay(reading.timestamp));
          break;
        case "week":
          readingTimeKey = getTime(startOfWeek(reading.timestamp));
          break;
        case "year":
          readingTimeKey = getTime(startOfMonth(reading.timestamp));
          break;
        default:
          readingTimeKey = reading.timestamp.getTime();
      }

      if (readingTimeKey === timeKey) {
        aggregatedReadings.push(reading);
        hasReadings = true;
      }
    }

    // Add accumulated reading or 0 if no readings found
    if (!hasReadings) {
      aggregatedReadings.push({
        timestamp: new Date(timeKey),
        value: 0,
      });
    }

    // Increment the date
    currentDate = incrementDate(currentDate);
  }

  return aggregatedReadings;
};

export { aggregateReadings };
