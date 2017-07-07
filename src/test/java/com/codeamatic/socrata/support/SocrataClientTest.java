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
  private List<String> sameDayDateList = new ArrayList<>();
  private List<String> diffDateDateList = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    sameDayDateList.add("2011-09-22" + TIME_START);
    sameDayDateList.add("2011-09-22" + TIME_END);

    diffDateDateList.add("2011-09-22" + TIME_START);
    diffDateDateList.add("2011-09-24" + TIME_END);
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
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, sameDayDateList);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, diffDateDateList);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDate() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("northside", sameDayDateList);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("avondale", diffDateDateList);

    assertTrue(crimeReportList.size() > 0);
  }
}
