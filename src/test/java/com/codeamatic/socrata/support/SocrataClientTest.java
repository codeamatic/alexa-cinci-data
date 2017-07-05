package com.codeamatic.socrata.support;

import com.codeamatic.socrata.CrimeReport;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SocrataClientTest {

  private static String API_TOKEN = null;

  private static String API_URL = "https://data.cincinnati-oh.gov/resource/4qzi-nepn.json";

  @Test
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
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, Arrays.asList("2011-09-22"));

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<String> dates = Arrays.asList("2011-09-22", "2011-09-24");
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports(null, dates);

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDate() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("northside", Arrays.asList("2011-09-22"));

    assertTrue(crimeReportList.size() > 0);
  }

  @Test
  public void testCrimeReportNeighborhoodDateRange() {
    SocrataClient socrataClient = new SocrataClient(API_TOKEN, API_URL);
    List<String> dates = Arrays.asList("2011-09-22", "2011-09-24");
    List<CrimeReport> crimeReportList = socrataClient.getCrimeReports("avondale", dates);

    assertTrue(crimeReportList.size() > 0);
  }
}
