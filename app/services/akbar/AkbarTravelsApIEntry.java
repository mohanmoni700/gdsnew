package services.akbar;

import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.FareCheckRulesResponse;
import dto.refund.AkbarCancelOrRefundRequest;
import dto.refund.GetRefundAmountRequest;

import java.util.Map;

public interface AkbarTravelsApIEntry {

    FlightItinerary getFreeBaggageInfo(TravellerMasterInfo travellerMasterInfo);

    Map<String, FareCheckRulesResponse> getFareRuleInfo(TravellerMasterInfo travellerMasterInfo);

    PNRResponse checkFareChangeAndFlightAvailability(TravellerMasterInfo travellerMasterInfo);

    PNRResponse generatePnr(TravellerMasterInfo travellerMasterInfo);

    IssuanceResponse completePaymentAndIssueTicket(IssuanceRequest issuanceRequest);

    AncillaryServicesResponse getPaidAncillaryAtPaxInfoPage(TravellerMasterInfo travellerMasterInfo);

    TicketCheckEligibilityRes getRefundableAmount(GetRefundAmountRequest getRefundAmountRequest);

    TicketProcessRefundRes confirmRefund(AkbarCancelOrRefundRequest akbarCancelOrRefundRequest);

}
