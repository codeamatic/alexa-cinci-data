package com.codeamatic.socrata.support;

import com.codeamatic.socrata.CrimeReport;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SocrataClientTest extends TestCase {

  private static final String API_TOKEN = null;
  private static final String API_URL = "https://data.cincinnati-oh.gov/resource/4qzi-nepn.json";
  private static final String TIME_START = "T00:00:00.000";
  private static final String TIME_END = "T23:59:59.999";
  private String[] sameDayDatesArray = new String[2];
  private String[] diffDateDateArray = new String[2];

  @Override
  protected void setUp() throws Exception {
    sameDayDatesArray[0] ="2011-09-22" + TIME_START;
    //sameDayDatesArray[1] ="2011-09-22" + TIME_END;

    diffDateDateArray[0] = "2011-09-22" + TIME_START;
    diffDateDateArray[1] = "2011-09-24" + TIME_END;
  }

  public void testCrimeReportBase() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports();

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportAll() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("all", null);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhood() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("avondale", null);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportSingleDate() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, sameDayDatesArray);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, diffDateDateArray);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDate() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("northside", sameDayDatesArray);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("avondale", diffDateDateArray);

    assertTrue(crimeReportList.size() > 0);
  }

  public void testFilterCrimeReportNeighborhoodDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("avondale", diffDateDateArray);

    assertTrue(crimeReportList.size() > 0);
  }
}
