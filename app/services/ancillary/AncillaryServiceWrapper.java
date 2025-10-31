package services.ancillary;

import com.amadeus.xml.pnracc_14_1_1a.PNRReply;
import com.compassites.model.AncillaryServicesResponse;
import com.compassites.model.PNRResponse;
import dto.ancillary.AncillaryBookingRequest;
import dto.ancillary.AncillaryBookingResponse;
import com.compassites.model.traveller.TravellerMasterInfo;
import models.AncillaryServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.akbar.AkbarTravelsApIEntry;
import services.indigo.IndigoFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AncillaryServiceWrapper implements AncillaryService {


    @Autowired
    AmadeusAncillaryService amadeusAncillaryService;

    @Autowired
    TravelomatixExtraService travelomatixExtraService;

    @Autowired
    private IndigoFlightService indigoFlightService;

    @Autowired
    private AkbarTravelsApIEntry akbarTravelsApIEntry;

    @Override
    public AncillaryServicesResponse getAdditionalBaggageInfoStandalone(AncillaryServiceRequest ancillaryServiceRequest) {

        AncillaryServicesResponse ancillaryServicesResponse = null;

        if (ancillaryServiceRequest.getProvider().equalsIgnoreCase("Amadeus")) {
            ancillaryServicesResponse = amadeusAncillaryService.additionalBaggageInformationStandalone(ancillaryServiceRequest);
        }

        return ancillaryServicesResponse;
    }

    @Override
    public AncillaryServicesResponse getMealsInfoStandalone(AncillaryServiceRequest ancillaryServiceRequest) {

        AncillaryServicesResponse ancillaryServicesResponse = null;

        if (ancillaryServiceRequest.getProvider().equalsIgnoreCase("Amadeus")) {
            ancillaryServicesResponse = amadeusAncillaryService.additionalMealsInformationStandalone(ancillaryServiceRequest);
        }

        return ancillaryServicesResponse;
    }

    @Override
    public Map<String, List<AncillaryBookingResponse>> getAncillaryBaggageConfirm(AncillaryBookingRequest ancillaryBookingRequest) {

        String provider = ancillaryBookingRequest.getProvider();

        Map<String, List<AncillaryBookingResponse>> ancillaryBookingResponse = null;
        if (provider.equalsIgnoreCase("Amadeus")) {
            ancillaryBookingResponse = amadeusAncillaryService.getpaymentConfirmAncillaryServices(ancillaryBookingRequest);
        }

        return ancillaryBookingResponse;
    }

    @Override
    public AncillaryServicesResponse getTmxExtraServices(String resultToken, String reResulttoken, String journeyType, Boolean isLCC) {
        AncillaryServicesResponse ancillaryServicesResponse = null;
        ancillaryServicesResponse = travelomatixExtraService.getExtraServicesfromTmx(resultToken, reResulttoken, journeyType, isLCC);
        return ancillaryServicesResponse;
    }

    @Override
    public AncillaryServicesResponse getAvailableAncillaryServices(TravellerMasterInfo travellerMasterInfo) {
        AncillaryServicesResponse ancillaryServicesResponse = null;
        ancillaryServicesResponse = indigoFlightService.getAvailableAncillaryServices(travellerMasterInfo);
        return ancillaryServicesResponse;
    }


    @Override
    public PNRResponse getFreeMealsAndSeatsConfirm(TravellerMasterInfo travellerMasterInfo) {
        PNRResponse pnrResponse = amadeusAncillaryService.getFreeMealsAndSeatsConfirm(travellerMasterInfo);
        return pnrResponse;
    }

    @Override
    public AncillaryServicesResponse getPaidAncillaryAtPaxInfoPage(TravellerMasterInfo travellerMasterInfo) {
        return akbarTravelsApIEntry.getPaidAncillaryAtPaxInfoPage(travellerMasterInfo);
    }

}