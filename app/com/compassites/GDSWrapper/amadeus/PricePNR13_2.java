package com.compassites.GDSWrapper.amadeus;

import com.amadeus.xml.pnracc_14_1_1a.PNRReply;
import com.amadeus.xml.pnradd_13_2_1a.CompanyIdentificationType;
import com.amadeus.xml.tpcbrq_12_4_1a.*;
import com.amadeus.xml.tpcbrq_13_2_1a.*;
import com.amadeus.xml.tpcbrq_13_2_1a.FarePricePNRWithBookingClass;
import com.amadeus.xml.tpcbrq_13_2_1a.ReferenceInfoType;
import com.amadeus.xml.tpcbrq_13_2_1a.ReferencingDetailsType;
import com.amadeus.xml.tpuprq_18_1_1a.FarePriceUpsellPNR;
import com.compassites.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PricePNR13_2 {

    public com.amadeus.xml.tpcbrq_13_2_1a.FarePricePNRWithBookingClass getPNRPricingOption13_2(String carrierCode, PNRReply pnrReply, boolean isSeamen,
                                                                                               boolean isDomesticFlight, FlightItinerary flightItinerary,
                                                                                               List<AirSegmentInformation> airSegmentList, boolean isSegmentWisePricing, boolean isAddBooking, boolean isSplitTicket, int journeyIndex) {

    FarePricePNRWithBookingClass pricePnr = new FarePricePNRWithBookingClass();
    List<FarePricePNRWithBookingClass.PricingOptionGroup> pricingGroups = new ArrayList<>();

    //RP
    FarePricePNRWithBookingClass.PricingOptionGroup groupRP = new FarePricePNRWithBookingClass.PricingOptionGroup();
    PricingOptionKeyType keyRP = new PricingOptionKeyType();
    keyRP.setPricingOptionKey("RP");
    groupRP.setPricingOptionKey(keyRP);
    pricingGroups.add(groupRP);

    // RU
    FarePricePNRWithBookingClass.PricingOptionGroup groupRU = new FarePricePNRWithBookingClass.PricingOptionGroup();
    PricingOptionKeyType keyRU = new PricingOptionKeyType();
    keyRU.setPricingOptionKey("RU");
    groupRU.setPricingOptionKey(keyRU);
    pricingGroups.add(groupRU);

    // RLO
    FarePricePNRWithBookingClass.PricingOptionGroup groupRLO = new FarePricePNRWithBookingClass.PricingOptionGroup();
    PricingOptionKeyType keyRLO = new PricingOptionKeyType();
    keyRLO.setPricingOptionKey("RLO");
    groupRLO.setPricingOptionKey(keyRLO);
    pricingGroups.add(groupRLO);

    //  VC
    FarePricePNRWithBookingClass.PricingOptionGroup groupVC = new FarePricePNRWithBookingClass.PricingOptionGroup();
    PricingOptionKeyType keyVC = new PricingOptionKeyType();
    keyVC.setPricingOptionKey("VC");
    groupVC.setPricingOptionKey(keyVC);

    com.amadeus.xml.tpcbrq_13_2_1a.TransportIdentifierType carrierInfo = new com.amadeus.xml.tpcbrq_13_2_1a.TransportIdentifierType();
    com.amadeus.xml.tpcbrq_13_2_1a.CompanyIdentificationTypeI companyIdentification = new com.amadeus.xml.tpcbrq_13_2_1a.CompanyIdentificationTypeI();
    companyIdentification.setOtherCompany(carrierCode);
    carrierInfo.setCompanyIdentification(companyIdentification);
    groupVC.setCarrierInformation(carrierInfo);
    pricingGroups.add(groupVC);

    // 5. Get segment reference
    String segmentRef = null;
    if (!airSegmentList.isEmpty()) {
        String firstSegKey = airSegmentList.get(0).getFromLocation() +
                airSegmentList.get(0).getToLocation();

        for (PNRReply.OriginDestinationDetails originDest : pnrReply.getOriginDestinationDetails()) {
            for (PNRReply.OriginDestinationDetails.ItineraryInfo itinerary : originDest.getItineraryInfo()) {
                if ("AIR".equalsIgnoreCase(itinerary.getElementManagementItinerary().getSegmentName())) {
                    String segKey = itinerary.getTravelProduct().getBoardpointDetail().getCityCode() +
                            itinerary.getTravelProduct().getOffpointDetail().getCityCode();
                    if (segKey.equalsIgnoreCase(firstSegKey)) {
                        segmentRef = itinerary.getElementManagementItinerary()
                                .getReference().getNumber().toString();
                        break;
                    }
                }
            }
            if (segmentRef != null) break;
        }
    }

    pricePnr.getPricingOptionGroup().addAll(pricingGroups);
    return pricePnr;
 }
}
