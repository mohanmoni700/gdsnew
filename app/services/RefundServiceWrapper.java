package services;

import com.compassites.model.TicketCheckEligibilityRes;
import com.compassites.model.TicketProcessRefundRes;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.IndigoPaxNumber;
import dto.refund.AkbarCancelOrRefundRequest;
import dto.refund.GetRefundAmountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.akbar.AkbarTravelsApIEntry;
import services.indigo.IndigoFlightService;


import java.util.List;

@Service
public class RefundServiceWrapper {

    @Autowired
    public RefundService amadeusRefundService;
    @Autowired
    private IndigoFlightService indigoFlightService;

    @Autowired
    private AkbarTravelsApIEntry akbarTravelsApIEntry;

    public TicketCheckEligibilityRes checkTicketEligibility(String provider, String gdsPNR, String searchOfficeId, String ticketingOfficeId, Boolean isSeamen, GetRefundAmountRequest getRefundAmountRequest) {
        TicketCheckEligibilityRes ticketCheckEligibilityRes = null;
        if (provider.equalsIgnoreCase("Amadeus")) {
            ticketCheckEligibilityRes = amadeusRefundService.checkTicketEligibility(gdsPNR, searchOfficeId, ticketingOfficeId);
        } else if (provider.equalsIgnoreCase("Indigo")) {
            ticketCheckEligibilityRes = indigoFlightService.processFullCancellation(gdsPNR, searchOfficeId, ticketingOfficeId, null, isSeamen);
        } else if (provider.equalsIgnoreCase("Akbar")) {
            ticketCheckEligibilityRes = akbarTravelsApIEntry.getRefundableAmount(getRefundAmountRequest);
        }
        return ticketCheckEligibilityRes;
    }

    public TicketProcessRefundRes processFullRefund(String provider, String gdsPNR, String searchOfficeId, String ticketingOfficeId, TravellerMasterInfo travellerMasterInfo, AkbarCancelOrRefundRequest akbarCancelOrRefundRequest) {
        TicketProcessRefundRes ticketProcessRefundRes = null;
        if (provider.equalsIgnoreCase("Amadeus")) {
            ticketProcessRefundRes = amadeusRefundService.processFullRefund(gdsPNR, searchOfficeId, ticketingOfficeId);
        } else if (provider.equalsIgnoreCase("Indigo")) {
            ticketProcessRefundRes = indigoFlightService.processFullRefund(gdsPNR, searchOfficeId, ticketingOfficeId, travellerMasterInfo);
        } else if (provider.equalsIgnoreCase("Akbar")) {
            ticketProcessRefundRes = akbarTravelsApIEntry.confirmRefund(akbarCancelOrRefundRequest);
        }
        return ticketProcessRefundRes;
    }

    public TicketCheckEligibilityRes checkPartRefundTicketEligibility(String provider, String gdsPNR, List<String> ticketList, String searchOfficeId, String ticketingOfficeId, List<String> ticketIdsList, Boolean isSeamen, GetRefundAmountRequest getRefundAmountRequest) {
        TicketCheckEligibilityRes ticketCheckEligibilityRes = null;

        if (provider.equalsIgnoreCase("Amadeus")) {
            ticketCheckEligibilityRes = amadeusRefundService.checkPartRefundTicketEligibility(ticketList, gdsPNR, searchOfficeId, ticketingOfficeId);
        } else if (provider.equalsIgnoreCase("Indigo")) {
            ticketCheckEligibilityRes = indigoFlightService.processFullCancellation(gdsPNR, searchOfficeId, ticketingOfficeId, ticketIdsList, isSeamen);
        } else if (provider.equalsIgnoreCase("Akbar")) {
            getRefundAmountRequest.setPartial(true);
            ticketCheckEligibilityRes = akbarTravelsApIEntry.getRefundableAmount(getRefundAmountRequest);
        }
        return ticketCheckEligibilityRes;
    }

    public TicketProcessRefundRes processPartialRefund(String provider, String gdsPNR, List<String> ticketList, String searchOfficeId, String ticketingOfficeId, List<IndigoPaxNumber> indigoPaxNumbers,
                                                       TravellerMasterInfo travellerMasterInfo, AkbarCancelOrRefundRequest akbarCancelOrRefundRequest) {

        TicketProcessRefundRes ticketProcessRefundRes = null;
        if (provider.equalsIgnoreCase("Amadeus")) {
            ticketProcessRefundRes = amadeusRefundService.processPartialRefund(ticketList, gdsPNR, searchOfficeId, ticketingOfficeId);
        } else if (provider.equalsIgnoreCase("Indigo")) {
            ticketProcessRefundRes = indigoFlightService.processPartialRefund(gdsPNR, searchOfficeId, ticketingOfficeId, ticketList, indigoPaxNumbers, travellerMasterInfo);
        } else if (provider.equalsIgnoreCase("Akbar")) {
            ticketProcessRefundRes = akbarTravelsApIEntry.confirmRefund(akbarCancelOrRefundRequest);
        }
        return ticketProcessRefundRes;
    }


}
