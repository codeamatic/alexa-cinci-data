package com.codeamatic;

import com.codeamatic.exceptions.DateRangeException;
import com.codeamatic.exceptions.DateStringNotSupportedException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateStringUtilTest {

  @Test
  public void testLastWeekFormatted() throws DateStringNotSupportedException, DateRangeException {
    assertNotNull(DateStringUtil.getFormattedDates("last week"));
  }

}
