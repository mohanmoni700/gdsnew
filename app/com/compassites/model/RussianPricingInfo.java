package com.compassites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RussianPricingInfo {

    private BigDecimal equivAmount;
    private BigDecimal perPaxTotalAmount;
    private String farePriceTypeCode;
    private String priceCode;
    private String priceName;
    private String priceClassId;
    private String rbd;
    private String PaxSegmentRefID;
    private String offerItemID;
    private String offerID;
    private String fareBasis;
    private List<String> originDest;
    private List<String> paxRefId;
    private BigDecimal totalEquivAmount;
    private BigDecimal totalPrice;
    private List<Map<String, String>> segmentFareDetails;


    public BigDecimal getEquivAmount() {
        return equivAmount;
    }

    public void setEquivAmount(BigDecimal equivAmount) {
        this.equivAmount = equivAmount;
    }

    public String getFarePriceTypeCode() {
        return farePriceTypeCode;
    }

    public void setFarePriceTypeCode(String farePriceTypeCode) {
        this.farePriceTypeCode = farePriceTypeCode;
    }

    public String getPriceCode() {
        return priceCode;
    }

    public void setPriceCode(String priceCode) {
        this.priceCode = priceCode;
    }

    public String getPriceName() {
        return priceName;
    }

    public void setPriceName(String priceName) {
        this.priceName = priceName;
    }

    public String getPriceClassId() {
        return priceClassId;
    }

    public void setPriceClassId(String priceClassId) {
        this.priceClassId = priceClassId;
    }

    public String getRbd() {
        return rbd;
    }

    public void setRbd(String rbd) {
        this.rbd = rbd;
    }

    public List<String> getPaxRefId() {
        return paxRefId;
    }

    public void setPaxRefId(List<String> paxRefId) {
        this.paxRefId = paxRefId;
    }

    public String getPaxSegmentRefID() {
        return PaxSegmentRefID;
    }

    public void setPaxSegmentRefID(String paxSegmentRefID) {
        PaxSegmentRefID = paxSegmentRefID;
    }

    public String getOfferItemID() {
        return offerItemID;
    }

    public void setOfferItemID(String offerItemID) {
        this.offerItemID = offerItemID;
    }

    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }

    public String getFareBasis() {
        return fareBasis;
    }

    public void setFareBasis(String fareBasis) {
        this.fareBasis = fareBasis;
    }


    public BigDecimal getPerPaxTotalAmount() {
        return perPaxTotalAmount;
    }

    public void setPerPaxTotalAmount(BigDecimal perPaxTotalAmount) {
        this.perPaxTotalAmount = perPaxTotalAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalEquivAmount() {
        return totalEquivAmount;
    }

    public void setTotalEquivAmount(BigDecimal totalEquivAmount) {
        this.totalEquivAmount = totalEquivAmount;
    }

    public List<Map<String, String>> getSegmentFareDetails() {
        return segmentFareDetails;
    }

    public void setSegmentFareDetails(List<Map<String, String>> segmentFareDetails) {
        this.segmentFareDetails = segmentFareDetails;
    }

    public List<String> getOriginDest() {
        return originDest;
    }

    public void setOriginDest(List<String> originDest) {
        this.originDest = originDest;
    }
}
