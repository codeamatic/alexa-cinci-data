package com.codeamatic.socrata.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.codeamatic.CincyDataSpeechlet;
import com.codeamatic.DateStringUtil;
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.Socrata;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the Socrata service interface.
 */
public class SocrataClient implements Socrata {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);

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
    CrimeReport[] crimeReports = null;

    if(dates[1] == null) {
      dates[1] = LocalDate.now().toString() + DateStringUtil.TIME_END;
    }

    String dateRangeBegin = null;
    String dateRangeEnd = null;

    dateRangeBegin = dates[0];
    dateRangeEnd = dates[1];

    String query = this.getServiceQuery(neighborhood, dateRangeBegin, dateRangeEnd);
    String urlString = this.serviceUrl + query;

    Gson gson = gsonBuilder.create();

    try {
      crimeReports = gson.fromJson(IOUtils.toString(new URL(urlString)), CrimeReport[].class);
    } catch(IOException ex) {
      log.error("Exception:" + ex);
      return Collections.emptyList();
    }

    return Arrays.asList(crimeReports);
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

    // Only a single date has been queried
    if(dateRangeBegin != null && dateRangeEnd != null) {
      where = (where.isEmpty()) ? where : where + " AND ";
      where += "occurredon >= '" + dateRangeBegin + "' AND occurredon <= '" + dateRangeEnd + "'";
    }

    where = (where.isEmpty()) ? where : appToken + "&$where=" + where;
    query = query + where;

    return query.replace(" ", "%20");
  }
}
