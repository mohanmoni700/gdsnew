package dto.refund;

import com.compassites.model.traveller.TravellerMasterInfo;

public class IndigoRefundRequest {
    private String gdsPNR;
    private String searchOfficeId;
    private String ticketingOfficeId;
    private TravellerMasterInfo travellerMasterInfo;

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
