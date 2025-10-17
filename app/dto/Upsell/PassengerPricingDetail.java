package dto.Upsell;

import com.compassites.model.PassengerTypeCode;

import java.math.BigDecimal;
import java.util.List;

public class PassengerPricingDetail {
    private String passengerType;
    private int passengerCount;
    private BigDecimal baseFare;
    private BigDecimal totalFare;
    private String currency;
    private String baggageAllowance;
    private String fareBasis;
    private List<UpsellSegmentDetail> segments;

    public List<UpsellSegmentDetail> getSegments() {
        return segments;
    }

    public void setSegments(List<UpsellSegmentDetail> segments) {
        this.segments = segments;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public PassengerTypeCode getPassengerTypeCode() {
        return PassengerTypeCode.valueOf(passengerType.toUpperCase());
    }

    public void setPassengerTypeCode(PassengerTypeCode code) {
        this.passengerType = code.name();
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(String baggageAllowance) {
        this.baggageAllowance = baggageAllowance;
    }

    public String getFareBasis() {
        return fareBasis;
    }

    public void setFareBasis(String fareBasis) {
        this.fareBasis = fareBasis;
    }

    public static PassengerTypeCode updatePassengerTypeCode(PassengerTypeCode passengerTypeCode, boolean seamen) {
        if (seamen) {
            return PassengerTypeCode.SEA;
        }

        if (passengerTypeCode == null) {
            return null;
        }

        switch (passengerTypeCode) {
            case ADT:
                return PassengerTypeCode.ADT;
            case CHD:
            case CH:
                return PassengerTypeCode.CHD;
            case INF:
            case IN:
                return PassengerTypeCode.INF;
            default:
                return passengerTypeCode;
        }
    }

}
