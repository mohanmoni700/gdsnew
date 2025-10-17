package dto.Upsell;

import com.compassites.model.FlightItinerary;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RbdUpsellReqDto {

    private FlightItinerary flightItinerary;

    @JsonProperty("isSeamen")
    private boolean isSeamen;

    private List<String> cabinClass;

    private String officeId;

    private String journeyType;

    private int adultCount;

    private int childCount;

    private int infantCount;


    public FlightItinerary getFlightItinerary() {
        return flightItinerary;
    }

    public void setFlightItinerary(FlightItinerary flightItinerary) {
        this.flightItinerary = flightItinerary;
    }

    public boolean isSeamen() {
        return isSeamen;
    }

    public void setSeamen(boolean seamen) {
        isSeamen = seamen;
    }

    public List<String> getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(List<String> cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getJourneyType() {
        return journeyType;
    }

    public void setJourneyType(String journeyType) {
        this.journeyType = journeyType;
    }

    public int getAdultCount() {
        return adultCount;
    }

    public void setAdultCount(int adultCount) {
        this.adultCount = adultCount;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getInfantCount() {
        return infantCount;
    }

    public void setInfantCount(int infantCount) {
        this.infantCount = infantCount;
    }

}
