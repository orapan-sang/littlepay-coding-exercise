package com.littlepay.model;

import java.math.BigDecimal;

/**
 * A class represents a fare rule row from CSV file
 */
public class FareRule {
  private String fromStopId;
  private String toStopId;
  private BigDecimal fareAmount;

  public FareRule() {

  }

  public FareRule(String fromStopId, String toStopId, BigDecimal fareAmount) {
    this.fromStopId = fromStopId;
    this.toStopId = toStopId;
    this.fareAmount = fareAmount;
  }

  public String getFromStopId() {
    return fromStopId;
  }

  public void setFromStopId(String fromStopId) {
    this.fromStopId = fromStopId;
  }

  public String getToStopId() {
    return toStopId;
  }

  public void setToStopId(String toStopId) {
    this.toStopId = toStopId;
  }


  public BigDecimal getFareAmount() {
    return fareAmount;
  }

  public void setFareAmount(BigDecimal fareAmount) {
    this.fareAmount = fareAmount;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FareRule{");
    sb.append("fromStopId='").append(fromStopId).append('\'');
    sb.append(", toStopId='").append(toStopId).append('\'');
    sb.append(", fareAmount=").append(fareAmount);
    sb.append('}');
    return sb.toString();
  }
}