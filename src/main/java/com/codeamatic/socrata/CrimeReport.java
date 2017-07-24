package com.codeamatic.socrata;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a Socrata Crime Report
 */
public class CrimeReport {

  private String count;

  @SerializedName("addressstate")
  private String addressState;

  private String beat;

  @SerializedName("block_begin")
  private String blockBegin;

  @SerializedName("block_end")
  private String blockEnd;

  @SerializedName("casereportno")
  private String caseReportNo;

  private String city;

  @SerializedName("datatypeid")
  private String dataTypeId;

  private String district;

  private String neighborhood;

  @SerializedName("occurredon")
  private String occurredOn;

  private String offense;

  @SerializedName("offenseno")
  private String offenseNo;

  private String orc;

  @SerializedName("reportedbyofficer")
  private String reportedByOfficer;

  @SerializedName("reportedbyofficerbadgeno")
  private String reportedByOfficeBadgeNo;

  @SerializedName("reportedon")
  private String reportedOn;

  @SerializedName("reportingarea")
  private String reportingArea;

  private String sname;

  private String ucr;

  public String getCount() { return count; }

  public void setCount(String count) { this.count = count; }

  public String getAddressState() {
    return addressState;
  }

  public void setAddressState(String addressState) {
    this.addressState = addressState;
  }

  public String getBeat() {
    return beat;
  }

  public void setBeat(String beat) {
    this.beat = beat;
  }

  public String getBlockBegin() {
    return blockBegin;
  }

  public void setBlockBegin(String blockBegin) {
    this.blockBegin = blockBegin;
  }

  public String getBlockEnd() {
    return blockEnd;
  }

  public void setBlockEnd(String blockEnd) {
    this.blockEnd = blockEnd;
  }

  public String getCaseReportNo() {
    return caseReportNo;
  }

  public void setCaseReportNo(String caseReportNo) {
    this.caseReportNo = caseReportNo;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getDataTypeId() {
    return dataTypeId;
  }

  public void setDataTypeId(String dataTypeId) {
    this.dataTypeId = dataTypeId;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  public String getOccurredOn() {
    return occurredOn;
  }

  public void setOccurredOn(String occurredOn) {
    this.occurredOn = occurredOn;
  }

  public String getOffense() {
    return offense;
  }

  public void setOffense(String offense) {
    this.offense = offense;
  }

  public String getOffenseNo() {
    return offenseNo;
  }

  public void setOffenseNo(String offenseNo) {
    this.offenseNo = offenseNo;
  }

  public String getOrc() {
    return orc;
  }

  public void setOrc(String orc) {
    this.orc = orc;
  }

  public String getReportedByOfficer() {
    return reportedByOfficer;
  }

  public void setReportedByOfficer(String reportedByOfficer) {
    this.reportedByOfficer = reportedByOfficer;
  }

  public String getReportedByOfficeBadgeNo() {
    return reportedByOfficeBadgeNo;
  }

  public void setReportedByOfficeBadgeNo(String reportedByOfficeBadgeNo) {
    this.reportedByOfficeBadgeNo = reportedByOfficeBadgeNo;
  }

  public String getReportedOn() {
    return reportedOn;
  }

  public void setReportedOn(String reportedOn) {
    this.reportedOn = reportedOn;
  }

  public String getReportingArea() {
    return reportingArea;
  }

  public void setReportingArea(String reportingArea) {
    this.reportingArea = reportingArea;
  }

  public String getSname() {
    return sname;
  }

  public void setSname(String sname) {
    this.sname = sname;
  }

  public String getUcr() {
    return ucr;
  }

  public void setUcr(String ucr) {
    this.ucr = ucr;
  }
}
