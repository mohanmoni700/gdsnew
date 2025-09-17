package services.indigo;

import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.reissue.ReIssueSearchRequest;

public interface IndigoFlightService {
    public PNRResponse checkFareChangeAndAvailability(TravellerMasterInfo travellerMasterInfo);
    public PNRResponse generatePNR(TravellerMasterInfo travellerMasterInfo);
    public IssuanceResponse priceBookedPNR(IssuanceRequest issuanceRequest);
    public IssuanceResponse issueTicket(IssuanceRequest issuanceRequest);
    public AncillaryServicesResponse getAvailableAncillaryServices(TravellerMasterInfo travellerMasterInfo);

    SearchResponse getReissueSearchResponse(ReIssueSearchRequest reIssueSearchRequest);
}
