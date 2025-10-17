package dto.Upsell;

import com.compassites.model.PAXFareDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectedRbdOption {
    private String origin;
    private String destination;
    private String departureTime;
    private List<PAXFareDetails> paxFareDetailsList;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public List<PAXFareDetails> getPaxFareDetailsList() {
        return paxFareDetailsList;
    }

    public void setPaxFareDetailsList(List<PAXFareDetails> paxFareDetailsList) {
        this.paxFareDetailsList = paxFareDetailsList;
    }
}
