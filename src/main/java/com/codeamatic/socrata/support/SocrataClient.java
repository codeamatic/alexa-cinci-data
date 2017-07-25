package com.codeamatic.socrata.support;

import com.google.gson.GsonBuilder;

import com.codeamatic.CincyDataSpeechlet;
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.Socrata;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the Socrata service interface.
 */
public class SocrataClient implements Socrata {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);
  private static final String TIME_START = "T00:00:00.000";
  private static final String TIME_END = "T23:59:59.999";

  private String token;
  private String serviceUrl;
  private GsonBuilder gsonBuilder = new GsonBuilder();

  public SocrataClient(String token,  String serviceUrl) {
    this.token = token;
    this.serviceUrl = serviceUrl;
  }

  /**
   * {@inheritDoc}
   */
  public List<CrimeReport> getCrimeReports() {
    return this.getCrimeReports(null, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<CrimeReport> getCrimeReports(String neighborhood, String[] dates) {
    CrimeReport[] crimeReports;

    dates = prepareDates(dates);

    String query = this.getServiceQuery(neighborhood, dates[0], dates[1]);
    String urlString = this.serviceUrl + query;

    try {
      crimeReports = gsonBuilder.create().fromJson(IOUtils.toString(new URL(urlString)), CrimeReport[].class);
    } catch(IOException ex) {
      log.error("Exception:" + ex);
      return Collections.emptyList();
    }

    return Arrays.asList(crimeReports);
  }

  /**
   * Prepares a date array for being used to query for crime reports.
   *
   * Ensures that dates aren't null and that we default to "yesterday" if
   * a null set of dates have been passed.
   *
   * @param dates Array of two dates.
   * @return Array of dates
   */
  private String[] prepareDates(String[] dates) {

    if(dates == null || dates[0] == null) {
      LocalDate yesterday = LocalDate.now().minus(Period.ofDays(1));
      dates = new String[2];
      dates[0] = yesterday.toString();
      dates[1] = dates[0];
    }

    if(dates[1] == null) {
      dates[1] = LocalDate.now().toString();
    }

    dates[0] += TIME_START;
    dates[1] += TIME_END;

    return dates;
  }

  /**
   * Builds a url query string to be used to filter the results of
   * a Socrata service request.
   *
   * @param neighborhood String the neighborhood
   * @param dateRangeBegin String the start date of a date range
   * @param dateRangeEnd String the ending date of a date range
   * @return query string for Socrata API
   */
  private String getServiceQuery(String neighborhood, String dateRangeBegin, String dateRangeEnd) {
    String appToken = (this.token != null) ? "&$$app_token=" + this.token : "";
    String query = "?$select=offense,COUNT(offense) as count&$group=offense&$order=count";
    String where = "";

    // specific neighborhood or all
    if(neighborhood != null && ! "all".equalsIgnoreCase(neighborhood)) {
      where += "neighborhood = '" + neighborhood.toUpperCase() + "'";
    }

    where = (where.isEmpty()) ? where : where + " AND ";
    where += "occurredon >= '" + dateRangeBegin + "' AND occurredon <= '" + dateRangeEnd + "'";

    where = (where.isEmpty()) ? where : appToken + "&$where=" + where;
    query = query + where;

    return query.replace(" ", "%20");
  }
}
