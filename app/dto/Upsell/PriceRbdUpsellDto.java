package dto.Upsell;

import com.compassites.model.FlightItinerary;
import com.compassites.model.Journey;
import com.compassites.model.PAXFareDetails;
import com.compassites.model.PricingInformation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceRbdUpsellDto {

    @JsonProperty("isSeamen")
    private boolean isSeamen;
    private int adultCount;
    private int childCount;
    private int infantCount;
    private String officeId;
    private List<Journey> journeyList;
    private SelectedRBDDetails selectedRBDDetails;
    private PricingInformation pricingInformation;

    private FlightItinerary flightItinerary;
    private List<SelectedRbdOption> SelectedRbdOption;

    public boolean isSeamen() {
        return isSeamen;
    }

    public void setSeamen(boolean seamen) {
        isSeamen = seamen;
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

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public List<Journey> getJourneyList() {
        return journeyList;
    }

    public void setJourneyList(List<Journey> journeyList) {
        this.journeyList = journeyList;
    }


    public SelectedRBDDetails getSelectedRBDDetails() {
        return selectedRBDDetails;
    }

    public void setSelectedRBDDetails(SelectedRBDDetails selectedRBDDetails) {
        this.selectedRBDDetails = selectedRBDDetails;
    }

    public PricingInformation getPricingInformation() {
        return pricingInformation;
    }

    public void setPricingInformation(PricingInformation pricingInformation) {
        this.pricingInformation = pricingInformation;
    }

    public FlightItinerary getFlightItinerary() {
        return flightItinerary;
    }

    public void setFlightItinerary(FlightItinerary flightItinerary) {
        this.flightItinerary = flightItinerary;
    }

    public List<SelectedRbdOption> getSelectedRbdOption() {
        return SelectedRbdOption;
    }

    public void setSelectedRbdOption(List<SelectedRbdOption> selectedRbdOption) {
        SelectedRbdOption = selectedRbdOption;
    }
}
