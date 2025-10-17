package services;

import com.compassites.model.FlightItinerary;
import com.compassites.model.Journey;
import com.compassites.model.PAXFareDetails;
import dto.Upsell.*;

import java.util.List;
import java.util.Map;

public interface UpsellService {

    Map<String, Map<String, List<String>>> getRbdUpsellAvailability(RbdUpsellReqDto upsellRequest);

    RbdUpgradePriceResponse getRbdUpgradePriceDetails(PriceRbdUpsellDto requestDto);

    UpdatedItineraryResponse updateItineraryForSelectedRbd(PriceRbdUpsellDto requestDto);

}
