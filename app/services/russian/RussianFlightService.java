package services.russian;

import com.compassites.model.IssuanceRequest;
import com.compassites.model.IssuanceResponse;
import com.compassites.model.PNRResponse;
import com.compassites.model.traveller.TravellerMasterInfo;

public interface RussianFlightService {
     PNRResponse checkFareChangeAndAvailability(TravellerMasterInfo travellerMasterInfo);

    PNRResponse generatePNR(TravellerMasterInfo travellerMasterInfo);

    IssuanceResponse confirmAndIssueTicket(IssuanceRequest issuanceRequest);
}
