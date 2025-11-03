package dto.refund;

import com.compassites.model.traveller.TravellerMasterInfo;
import dto.IndigoPaxNumber;

import java.util.List;

public class IndigoRefundRequest {
    private String gdsPNR;
    private String searchOfficeId;
    private String ticketingOfficeId;
    private TravellerMasterInfo travellerMasterInfo;
    private List<String> ticketIdsList;
    private Boolean isSeamen;

    public Boolean getIsSeamen() {
        return isSeamen;
    }

    public void setIsSeamen(Boolean isSeamen) {
        this.isSeamen = isSeamen;
    }

    public List<String> getTicketIdsList() {
        return ticketIdsList;
    }

    public void setTicketIdsList(List<String> ticketIdsList) {
        this.ticketIdsList = ticketIdsList;
    }

    private List<IndigoPaxNumber> indigoPaxNumbers;

    public List<IndigoPaxNumber> getIndigoPaxNumbers() {
        return indigoPaxNumbers;
    }

    public void setIndigoPaxNumbers(List<IndigoPaxNumber> indigoPaxNumbers) {
        this.indigoPaxNumbers = indigoPaxNumbers;
    }

    public TravellerMasterInfo getTravellerMasterInfo() {
        return travellerMasterInfo;
    }

    public void setTravellerMasterInfo(TravellerMasterInfo travellerMasterInfo) {
        this.travellerMasterInfo = travellerMasterInfo;
    }

    public String getGdsPNR() {
        return gdsPNR;
    }

    public void setGdsPNR(String gdsPNR) {
        this.gdsPNR = gdsPNR;
    }

    public String getSearchOfficeId() {
        return searchOfficeId;
    }

    public void setSearchOfficeId(String searchOfficeId) {
        this.searchOfficeId = searchOfficeId;
    }

    public String getTicketingOfficeId() {
        return ticketingOfficeId;
    }

    public void setTicketingOfficeId(String ticketingOfficeId) {
        this.ticketingOfficeId = ticketingOfficeId;
    }
}
