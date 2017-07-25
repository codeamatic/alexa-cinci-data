package com.codeamatic;

import junit.framework.TestCase;

public class NeighborhoodTest extends TestCase {

  public void testNeighborhoodNotExist() {
    assertNull(Neighborhoods.getNeighborhood("Montgomery"));
  }

  public void testNeighborhoodExists() {
    assertEquals("Avondale", Neighborhoods.getNeighborhood("Avondale"));
  }

  public void testNeighborhoodExistsMultipleSpaces() {
    // Socrata has Mount Washington as "Mount  Washington", so we have to be able
    // to send in Mount Washington (single space) and it be identified correctly.
    assertEquals("Mount  Adams", Neighborhoods.getNeighborhood("Mount Adams"));
  }

  public void testNeighborhoodExistsMountAbbreviation() {
    // Socrata has Mount Washington as "Mount  Washington", so we have to be able
    // to send in Mount Washington (single space) and it be identified correctly.
    assertEquals("Mt.  Washington", Neighborhoods.getNeighborhood("Mount Washington"));
  }

  public void testNeighborhoodExistsHyphens() {
    // Socrata has Over The Rhine as "Over-The-Rhine", so we have to be able
    // to send in Over-The-Rhine (hypens) and it be identified correctly.
    assertEquals("Over-The-Rhine", Neighborhoods.getNeighborhood("Over The Rhine"));
  }
}
