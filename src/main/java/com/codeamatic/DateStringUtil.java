package com.codeamatic;

import com.codeamatic.exceptions.DateRangeException;
import com.codeamatic.exceptions.DateStringNotSupportedException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class DateStringUtil {

  private static final List<String> dateStrings = new ArrayList<String>();

  private DateStringUtil() {}

  static {
    dateStrings.add("last week");
    dateStrings.add("last weekend");
    dateStrings.add("last month");
    dateStrings.add("last year");
    dateStrings.add("since last year");
    dateStrings.add("since last month");
    dateStrings.add("since last week");
    dateStrings.add("since sunday");
    dateStrings.add("since monday");
    dateStrings.add("since tuesday");
    dateStrings.add("since wednesday");
    dateStrings.add("since thursday");
    dateStrings.add("since friday");
    dateStrings.add("since saturday");
  }

  /**
   * Returns a start and end date for a date range given based on the date string.
   *
   * @param dateString String based date typically used when translating a date range in every conversation
   * @return String of two dates for starting and ending range
   * @throws DateStringNotSupportedException if a date string is provided that is not yet supported
   * @throws DateRangeException thrown if start date is greater than end date
   */
  public static String[] getFormattedDates(String dateString) throws DateStringNotSupportedException, DateRangeException {

    if(! dateStringExists(dateString)) {
      throw new DateStringNotSupportedException("Date String: " + dateString);
    }

    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate;
    LocalDate now = startDate;
    int dayOfWeek = now.getDayOfWeek().getValue();
    long minusDays = (dayOfWeek == 7) ? dayOfWeek : 7 + dayOfWeek;

    switch(dateString) {
      case "last week":
        // Start last week on Sunday
        startDate = now.minusDays(minusDays);
        // End week on Saturday
        endDate = startDate.plusDays(6);
        break;
      case "last weekend":
        startDate = now.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        // Adjust two days so that it reflects sunday and not the previous sunday
        endDate = startDate.plusDays(2);
        break;
      case "last month":
        startDate = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        break;
      case "last year":
        startDate = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear());
        endDate = startDate.with(TemporalAdjusters.lastDayOfYear());
        break;
      case "since last year":
        startDate = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear());
        break;
      case "since last month":
        startDate = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        break;
      case "since last week":
        // Set first day of last week to last Sunday
        startDate = now.minusDays(minusDays);
        break;
      case "since sunday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        break;
      case "since monday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        break;
      case "since tuesday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.TUESDAY));
        break;
      case "since wednesday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.WEDNESDAY));
        break;
      case "since thursday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        break;
      case "since friday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        break;
      case "since saturday":
        startDate = endDate.with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        break;
    }

    // Should never be the case, but test any how
    if(startDate.isAfter(endDate)) {
      throw new DateRangeException("Start date: " + startDate.toString() + " End date: " + endDate.toString());
    }

    String[] dates = new String[2];
    dates[0] = startDate.toString();
    dates[1] = endDate.toString();

    return dates;
  }

  private static boolean dateStringExists(String dateString) {
    return dateStrings.contains(dateString);
  }
}
