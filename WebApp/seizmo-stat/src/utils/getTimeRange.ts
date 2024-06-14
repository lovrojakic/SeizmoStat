import {
  startOfDay,
  startOfWeek,
  startOfMonth,
  addDays,
  addMonths,
  subWeeks,
} from "date-fns";

const getTimeRange = (
  range: string
): [Date, Date, "hour" | "day" | "week" | "year"] => {
  const now = new Date();
  let timeUnit: "hour" | "day" | "week" | "year";
  let startDate: Date;
  let endDate: Date;

  switch (range) {
    case "today":
      timeUnit = "hour";
      startDate = startOfDay(now);
      endDate = addDays(startDate, 1);
      break;
    case "lastWeek":
      timeUnit = "day";
      startDate = startOfWeek(now, { weekStartsOn: 1 }); // Assuming week starts on Monday
      endDate = addDays(startDate, 7);
      startDate = subWeeks(startDate, 1); // Adjust to last week
      break;
    case "lastMonth":
      timeUnit = "week";
      startDate = startOfWeek(startOfMonth(now));
      endDate = addMonths(startDate, 1);
      break;
    default:
      timeUnit = "year";
      startDate = new Date(0); // Epoch time
      endDate = now;
      break;
  }

  return [startDate, endDate, timeUnit];
};

export { getTimeRange };
