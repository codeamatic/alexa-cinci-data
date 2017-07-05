package com.codeamatic.socrata.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.codeamatic.CincyDataSpeechlet;
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.Socrata;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
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

  SocrataClient(String token,  String serviceUrl) {
    this.token = token;
    this.serviceUrl = serviceUrl;
  }

  /**
   * {@inheritDoc}
   */
  public List<CrimeReport> getCrimeReports() {
    return this.getCrimeReports(null, null, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<CrimeReport> getCrimeReports(String neighborhood, String date) {
    return this.getCrimeReports(neighborhood, date, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<CrimeReport> getCrimeReports(String neighborhood, String dateRangeBegin, String dateRangeEnd) {
    CrimeReport[] crimeReports = null;
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
    String query = "";

    // specific neighborhood or all
    if(neighborhood != null && ! "all".equalsIgnoreCase(neighborhood)) {
      query += "neighborhood = '" + neighborhood.toUpperCase() + "'";
    }

    // Only a single date has been queried
    if(dateRangeBegin != null && dateRangeEnd == null) {
      query = (query.isEmpty()) ? query : query + " AND ";
      query += "occurredon = '" + dateRangeBegin + "'";
    }
    // Looking for a date range
    else if(dateRangeBegin != null && dateRangeEnd != null) {
      query = (query.isEmpty()) ? query : query + " AND ";
      query += "occurredon >= '" + dateRangeBegin + "' AND occurredon <= '" + dateRangeEnd + "'";
    }

    query = (query.isEmpty()) ? query : "?$where=" + query + appToken;

    return query.replace(" ", "%20");
  }
}
