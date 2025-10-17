package dto.Upsell;

import com.compassites.model.Journey;
import com.compassites.model.PAXFareDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RbdUpsellFlightSearchDto {

    @JsonProperty("isSeamen")
    private boolean isSeamen;
    private int adultCount;
    private int childCount;
    private int infantCount;
    private String officeId;
    private List<Journey> journeyList;
//    private List<PAXFareDetails> paxFareDetailsList;
    private SelectedRBDDetails selectedRBDDetails;

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

//    public List<PAXFareDetails> getPaxFareDetailsList() {
//        return paxFareDetailsList;
//    }
//
//    public void setPaxFareDetailsList(List<PAXFareDetails> paxFareDetailsList) {
//        this.paxFareDetailsList = paxFareDetailsList;
//    }

    public SelectedRBDDetails getSelectedRBDDetails() {
        return selectedRBDDetails;
    }

    public void setSelectedRBDDetails(SelectedRBDDetails selectedRBDDetails) {
        this.selectedRBDDetails = selectedRBDDetails;
    }
}
