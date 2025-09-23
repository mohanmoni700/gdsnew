package services;

import com.compassites.model.TicketCheckEligibilityRes;
import com.compassites.model.TicketProcessRefundRes;
import com.compassites.model.traveller.TravellerMasterInfo;
import dto.IndigoPaxNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.indigo.IndigoFlightService;


import java.util.List;

@Service
public class RefundServiceWrapper {

    @Autowired
    public RefundService amadeusRefundService;
    @Autowired
    private IndigoFlightService indigoFlightService;

    public TicketCheckEligibilityRes checkTicketEligibility(String provider, String gdsPNR,String searchOfficeId, String ticketingOfficeId){
        TicketCheckEligibilityRes ticketCheckEligibilityRes = null;
      if(provider.equalsIgnoreCase("Amadeus")){
          ticketCheckEligibilityRes =  amadeusRefundService.checkTicketEligibility(gdsPNR,searchOfficeId,ticketingOfficeId);
      } else if(provider.equalsIgnoreCase("Indigo")){
          ticketCheckEligibilityRes =  indigoFlightService.processFullCancellation(gdsPNR,searchOfficeId,ticketingOfficeId,null);
      }
      return ticketCheckEligibilityRes;
    }

    public TicketProcessRefundRes processFullRefund(String provider, String gdsPNR, String searchOfficeId, String ticketingOfficeId, TravellerMasterInfo travellerMasterInfo){
        TicketProcessRefundRes ticketProcessRefundRes = null;
        if(provider.equalsIgnoreCase("Amadeus")){
            ticketProcessRefundRes =  amadeusRefundService.processFullRefund(gdsPNR,searchOfficeId, ticketingOfficeId);
        } else if(provider.equalsIgnoreCase("Indigo")){
            ticketProcessRefundRes =  indigoFlightService.processFullRefund(gdsPNR,searchOfficeId,ticketingOfficeId, travellerMasterInfo);
        }
        return ticketProcessRefundRes;
    }

    public TicketCheckEligibilityRes checkPartRefundTicketEligibility(String provider, String gdsPNR, List<String> ticketList,String searchOfficeId, String ticketingOfficeId, List<String> ticketIdsList){
        TicketCheckEligibilityRes ticketCheckEligibilityRes = null;

        if(provider.equalsIgnoreCase("Amadeus")){
            ticketCheckEligibilityRes =  amadeusRefundService.checkPartRefundTicketEligibility(ticketList,gdsPNR,searchOfficeId, ticketingOfficeId);
        } else if(provider.equalsIgnoreCase("Indigo")){
            ticketCheckEligibilityRes =  indigoFlightService.processFullCancellation(gdsPNR,searchOfficeId, ticketingOfficeId, ticketIdsList);
        }
        return ticketCheckEligibilityRes;
    }

    public TicketProcessRefundRes processPartialRefund(String provider, String gdsPNR,List<String> ticketList,String searchOfficeId, String ticketingOfficeId, List<IndigoPaxNumber> indigoPaxNumbers, TravellerMasterInfo travellerMasterInfo){
        TicketProcessRefundRes ticketProcessRefundRes = null;
        if(provider.equalsIgnoreCase("Amadeus")){
            ticketProcessRefundRes =  amadeusRefundService.processPartialRefund(ticketList,gdsPNR,searchOfficeId, ticketingOfficeId);
        } else if(provider.equalsIgnoreCase("Indigo")){
            ticketProcessRefundRes =  indigoFlightService.processPartialRefund(gdsPNR,searchOfficeId, ticketingOfficeId,ticketList, indigoPaxNumbers,travellerMasterInfo);
        }
        return ticketProcessRefundRes;
    }


}
