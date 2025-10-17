package dto.Upsell;


import com.compassites.model.ErrorMessage;
import com.compassites.model.PricingInformation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RbdUpgradePriceResponse {

    private String status;
    private ErrorMessage errorMessage;
    private String cabinClass;
    private String bookingClas;
    private int availableSeats;
    private String baggage;
    private BigDecimal fare;
    private String currency;
    private PricingInformation pricingInformation;
    private String fareBasis;
    private String segmentOrigin;
    private String segmentDestination;
    private String departureTime;

    @JsonProperty("isSeamen")
    private boolean isSeamen;

    public boolean isSeamen() {
        return isSeamen;
    }

    public void setSeamen(boolean seamen) {
        isSeamen = seamen;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getBookingClas() {
        return bookingClas;
    }

    public void setBookingClas(String bookingClas) {
        this.bookingClas = bookingClas;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getBaggage() {
        return baggage;
    }

    public void setBaggage(String baggage) {
        this.baggage = baggage;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public void setFare(BigDecimal fare) {
        this.fare = fare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PricingInformation getPricingInformation() {
        return pricingInformation;
    }

    public void setPricingInformation(PricingInformation pricingInformation) {
        this.pricingInformation = pricingInformation;
    }

    public String getFareBasis() {
        return fareBasis;
    }

    public void setFareBasis(String fareBasis) {
        this.fareBasis = fareBasis;
    }

    public String getSegmentOrigin() {
        return segmentOrigin;
    }

    public void setSegmentOrigin(String segmentOrigin) {
        this.segmentOrigin = segmentOrigin;
    }

    public String getSegmentDestination() {
        return segmentDestination;
    }

    public void setSegmentDestination(String segmentDestination) {
        this.segmentDestination = segmentDestination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }
}
