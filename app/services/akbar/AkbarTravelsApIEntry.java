package services.akbar;

import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.FareCheckRulesResponse;

import java.util.Map;

public interface AkbarTravelsApIEntry {

    FlightItinerary getFreeBaggageInfo(TravellerMasterInfo travellerMasterInfo);

    Map<String, FareCheckRulesResponse> getFareRuleInfo(TravellerMasterInfo travellerMasterInfo);

    PNRResponse checkFareChangeAndFlightAvailability(TravellerMasterInfo travellerMasterInfo);

    PNRResponse generatePnr(TravellerMasterInfo travellerMasterInfo);

    IssuanceResponse completePaymentAndIssueTicket(IssuanceRequest issuanceRequest);

    AncillaryServicesResponse getPaidAncillaryAtPaxInfoPage(TravellerMasterInfo travellerMasterInfo);
}
