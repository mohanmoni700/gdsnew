package services.akbar;

import com.compassites.model.FlightItinerary;
import com.compassites.model.IssuanceRequest;
import com.compassites.model.IssuanceResponse;
import com.compassites.model.PNRResponse;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.FareCheckRulesResponse;

import java.util.Map;

public interface AkbarTravelsApIEntry {

    FlightItinerary getFreeBaggageInfo(TravellerMasterInfo travellerMasterInfo);

    Map<String, FareCheckRulesResponse> getFareRuleInfo(TravellerMasterInfo travellerMasterInfo);

    PNRResponse checkFareChangeAndFlightAvailability(TravellerMasterInfo travellerMasterInfo);

    PNRResponse generatePnr(TravellerMasterInfo travellerMasterInfo);

    IssuanceResponse completePaymentAndIssueTicket(IssuanceRequest issuanceRequest);

}
