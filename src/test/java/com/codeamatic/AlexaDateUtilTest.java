package com.codeamatic;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class AlexaDateUtilTest  {

  @Test
  public void testAlreadyFormatted() {
    String testDate = "2017-03-02";
    assertEquals(testDate, AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testMonthFormatted() {
    String testDate = "2017-03";
    assertEquals("2017-03-01", AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testDecadeFormatted() {
    String testDate = "201X";
    assertEquals("2017-01-01", AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testYearFormatted() {
    String testDate = "2017";
    assertEquals("2017-01-01", AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testWeekFormatted() {
    String testDate = "2017-W23";
    assertEquals("2017-06-05", AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testWeekendFormatted() {
    String testDate = "2017-W23-WE";
    assertEquals(null, AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testSeasonFormatted() {
    String testDate = "2017-WI";
    assertEquals(null, AlexaDateUtil.getFormattedDate(testDate));
  }

  @Test
  public void testTodayFormatted() {
    String testDate = "PRESENT_REF";
    assertEquals(LocalDate.now().toString(), AlexaDateUtil.getFormattedDate(testDate));
  }
}
