package com.codeamatic.socrata;

import java.util.List;

/**
 * Simple API client for communicating with Socrata's open API's.
 */
public interface Socrata {

  /**
   * Retrieves a list of {@link CrimeReport} objects from the Socrata API.
   *
   * @return a list of crime reports
   */
  List<CrimeReport> getCrimeReports();

  /**
   * Retrieves a list of {@link CrimeReport} objects from the Socrata API
   * filtered by neighborhood and a single date.
   *
   * @return a list of crime reports
   */
  List<CrimeReport> getCrimeReports(String neighborhood, String[] dates);
}
