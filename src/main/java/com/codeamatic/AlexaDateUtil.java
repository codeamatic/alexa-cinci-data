package com.codeamatic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Provides date and time utilities to convert Alexa date strings to a
 * format that Socrarta can handle.
 */
public class AlexaDateUtil {

  /**
   *  Normal Date: YYYY-MM-DD
   */
  private static final String REGEX_DATE_NORMAL = "^\\d{4}-\\d{2}-\\d{2}$";

  /**
   *  This month: YYYY-MM
   */
  private static final String REGEX_DATE_MONTH = "^\\d{4}-\\d{2}$";

  /**
   *  This decade: YYYX
   */
  private static final String REGEX_DATE_DECADE = "^\\d{3}X$";

  /**
   * Year: YYYY
   */
  private static final String REGEX_DATE_YEAR = "^\\d{4}";

  /**
   * Week: YYYY-Www
   */
  private static final String REGEX_DATE_WEEK = "^\\d{4}-W\\d{2}$";

  /**
   * Weekend: YYYY-Www-WE
   * Date ranges aren't supported by this utility.  This REGEX is just a placeholder.
   */
  private static final String REGEX_DATE_WEEK_WEEKEND = "^\\d{4}-W\\d{2}-WE$";

  /**
   *  Seasonal: YYYY-JJ (where JJ represents a two letter season specification)
   *  Future dates aren't supported.  This REGEX is just a placeholder.
   */
  private static final String REGEX_DATE_SEASON = "^\\d{4}-[A-Z]{2}$";

  /**
   *  Right now: PRESENT_REF
   */
  private static final String REGEX_DATE_NOW = "^PRESENT_REF$";

  /**
   * Characters to be added (if necessary) to a date string in order to
   * fulfill the requirements of a full date string.
   */
  private static final String MONTH_DAY_CONST = "-01";

  /**
   * Takes an Alexa date and converts it to a basic YYYY-MM-DD format.  If
   * the date is already in the required format, it is returned. as is.
   *
   * If not in the desired format, it is manipulated so that the day represents the first
   * day of the month or of the year or both.
   *
   * Example: 2016-03 => 2016-03-01
   *
   * @param alexaDate String date from Alexa
   * @return String formatted date or null if date can't be formatted
   */
  static String getFormattedDate(String alexaDate) {
    String formattedDate = null;

    if(alexaDate.matches(REGEX_DATE_NORMAL)) {
      // Already in the necessary format
      formattedDate = alexaDate;
    } else if(alexaDate.matches(REGEX_DATE_NOW)) {
      formattedDate = LocalDate.now().toString();
    } else if(alexaDate.matches(REGEX_DATE_MONTH)) {
      // YYYY-MM + -01
      formattedDate = alexaDate + MONTH_DAY_CONST;
    } else if(alexaDate.matches(REGEX_DATE_DECADE)) {
      String currentYear = Integer.toString(LocalDate.now().getYear());
      formattedDate = currentYear + MONTH_DAY_CONST + MONTH_DAY_CONST;
    } else if(alexaDate.matches(REGEX_DATE_YEAR)) {
      formattedDate = alexaDate + MONTH_DAY_CONST + MONTH_DAY_CONST;
    } else if(alexaDate.matches(REGEX_DATE_WEEK)) {
      // Default to first day of the week (Monday)
      LocalDate parsedDate = LocalDate.parse(alexaDate + "-1", DateTimeFormatter.ISO_WEEK_DATE);
      formattedDate = parsedDate.toString();
    }

    return formattedDate;
  }
}
