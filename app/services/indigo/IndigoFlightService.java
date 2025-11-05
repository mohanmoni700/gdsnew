package services.indigo;

import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.IndigoPaxNumber;
import dto.reissue.ReIssueConfirmationRequest;
import dto.reissue.ReIssueSearchRequest;

import java.util.List;

public interface IndigoFlightService {
    public PNRResponse checkFareChangeAndAvailability(TravellerMasterInfo travellerMasterInfo);
    public PNRResponse generatePNR(TravellerMasterInfo travellerMasterInfo);
    public IssuanceResponse priceBookedPNR(IssuanceRequest issuanceRequest);
    public IssuanceResponse issueTicket(IssuanceRequest issuanceRequest);
    public AncillaryServicesResponse getAvailableAncillaryServices(TravellerMasterInfo travellerMasterInfo);

    SearchResponse getReissueSearchResponse(ReIssueSearchRequest reIssueSearchRequest);
    public TicketCheckEligibilityRes processFullCancellation(String gdsPNR, String searchOfficeId, String ticketingOfficeId,List<String> ticketIdsList, Boolean isSeamen);
    public TicketProcessRefundRes processFullRefund(String gdsPNR, String searchOfficeId, String ticketingOfficeId,TravellerMasterInfo travellerMasterInfo);
    TicketCheckEligibilityRes checkPartRefundTicketEligibilityForIndigo(List<String> ticketList, String gdspnr, String searchOfficeId, String ticketingOfficeId, List<String> ticketIdsList);
    TicketProcessRefundRes processPartialRefund(String gdsPNR,String searchOfficeId, String ticketingOfficeId,List<String> ticketList, List<IndigoPaxNumber> indigoPaxNumbers,TravellerMasterInfo travellerMasterInfo);
    PNRResponse confirmReIssue(ReIssueConfirmationRequest reIssueConfirmationRequest);
}
