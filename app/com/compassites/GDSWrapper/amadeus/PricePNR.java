/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compassites.GDSWrapper.amadeus;

import com.amadeus.xml.pnracc_14_1_1a.ElementManagementSegmentType;
import com.amadeus.xml.pnracc_14_1_1a.PNRReply;
import com.amadeus.xml.tpcbrq_12_4_1a.*;
import com.compassites.model.AirSegmentInformation;
import com.compassites.model.FareJourney;
import com.compassites.model.FlightItinerary;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mahendra-singh
 */
public class PricePNR {
    public FarePricePNRWithBookingClass getPNRPricingOption(String carrierCode, PNRReply pnrReply, boolean isSeamen,
                                                            boolean isDomesticFlight, FlightItinerary flightItinerary,
                                                            List<AirSegmentInformation> airSegmentList, boolean isSegmentWisePricing, boolean isAddBooking, boolean isSplitTicket, int journeyIndex){

        FarePricePNRWithBookingClass pricepnr=new FarePricePNRWithBookingClass();
        CodedAttributeType overrideInformation = new CodedAttributeType();
        ReferenceInformationTypeI94605S paxSegReference = new ReferenceInformationTypeI94605S();
        ReferencingDetailsTypeI142222C refDetails = new ReferencingDetailsTypeI142222C();
        String airlineStr = play.Play.application().configuration().getString("vistara.airline.code");
        if(isSplitTicket) {
            isSeamen = flightItinerary.getJourneyList().get(journeyIndex).isSeamen();
            System.out.println("isSeamen getPNRPricingOption ------------ "+isSeamen);
        }
//        if(isSegmentWisePricing){
//            for(AirSegmentInformation airSegment : airSegmentList)  {
//                String key = airSegment.getFromLocation() + airSegment.getToLocation();
//                for(PNRReply.OriginDestinationDetails originDestinationDetails : pnrReply.getOriginDestinationDetails()) {
//                    for (PNRReply.OriginDestinationDetails.ItineraryInfo itineraryInfo : originDestinationDetails.getItineraryInfo()) {
//                        String segType = itineraryInfo.getElementManagementItinerary().getSegmentName();
//                        if(segType.equalsIgnoreCase("AIR")) {
//                            String segments = itineraryInfo.getTravelProduct().getBoardpointDetail().getCityCode()
//                                    + itineraryInfo.getTravelProduct().getOffpointDetail().getCityCode();
//                            //TODO for multicity the starting and ending segments may be same
//                            if (segments.equals(key)) {
//                                refDetails = new ReferencingDetailsTypeI142222C();
//                                refDetails.setRefQualifier("S");
//                                refDetails.setRefNumber(itineraryInfo.getElementManagementItinerary().getReference().getNumber());
//                                paxSegReference.getRefDetails().add(refDetails);
//                            }
//                        }
//                    }
//                }
//            }
//            pricepnr.setPaxSegReference(paxSegReference);
//        }


//        if(isDomesticFlight && !isSegmentWisePricing){
//            //int i = 1;
////            for(Journey journey : flightItinerary.getJourneys(isSeamen))  {
//            for(AirSegmentInformation airSegment : airSegmentList)  {
//                /*refDetails = new ReferencingDetailsTypeI142222C();
//                refDetails.setRefQualifier("S");
//                refDetails.setRefNumber(BigInteger.valueOf(i));
//                paxSegReference.getRefDetails().add(refDetails);
//                i = i + 1;*/
//                String key = airSegment.getFromLocation() + airSegment.getToLocation();
//                StringBuilder stringBuilder = new StringBuilder();
//                for(PNRReply.OriginDestinationDetails originDestinationDetails : pnrReply.getOriginDestinationDetails()) {
//                    for (PNRReply.OriginDestinationDetails.ItineraryInfo itineraryInfo : originDestinationDetails.getItineraryInfo()) {
//                        String segType = itineraryInfo.getElementManagementItinerary().getSegmentName();
//                        ElementManagementSegmentType elementManagementItinerary = itineraryInfo.getElementManagementItinerary();
//                        if(segType.equalsIgnoreCase("AIR")) {
//                            String segments = itineraryInfo.getTravelProduct().getBoardpointDetail().getCityCode()
//                                    + itineraryInfo.getTravelProduct().getOffpointDetail().getCityCode();
//                            if (segments.equals(key)) {
//                                refDetails = new ReferencingDetailsTypeI142222C();
//                                refDetails.setRefQualifier("S");
//                                refDetails.setRefNumber(elementManagementItinerary.getReference().getNumber());
//                                paxSegReference.getRefDetails().add(refDetails);
//                            }
//                        }
//                    }
//                }
//            }
//            pricepnr.setPaxSegReference(paxSegReference);
//        }

//        if(isAddBooking && !isDomesticFlight && !isSegmentWisePricing) {
//            for(AirSegmentInformation airSegment : airSegmentList)  {
//                String key = airSegment.getFromLocation() + airSegment.getToLocation();
//                for(PNRReply.OriginDestinationDetails originDestinationDetails : pnrReply.getOriginDestinationDetails()) {
//                    for (PNRReply.OriginDestinationDetails.ItineraryInfo itineraryInfo : originDestinationDetails.getItineraryInfo()) {
//                        String segType = itineraryInfo.getElementManagementItinerary().getSegmentName();
//                        if(segType.equalsIgnoreCase("AIR")) {
//                            String segments = itineraryInfo.getTravelProduct().getBoardpointDetail().getCityCode()
//                                    + itineraryInfo.getTravelProduct().getOffpointDetail().getCityCode();
//                            if (segments.equals(key)) {
//                                refDetails = new ReferencingDetailsTypeI142222C();
//                                refDetails.setRefQualifier("S");
//                                refDetails.setRefNumber(itineraryInfo.getElementManagementItinerary().getReference().getNumber());
//                                paxSegReference.getRefDetails().add(refDetails);
//                            }
//                        }
//                    }
//                }
//            }
//
//            pricepnr.setPaxSegReference(paxSegReference);
//        }

        for(AirSegmentInformation airSegment : airSegmentList)  {
            String key = airSegment.getFromLocation() + airSegment.getToLocation();
            for(PNRReply.OriginDestinationDetails originDestinationDetails : pnrReply.getOriginDestinationDetails()) {
                for (PNRReply.OriginDestinationDetails.ItineraryInfo itineraryInfo : originDestinationDetails.getItineraryInfo()) {
                    String segType = itineraryInfo.getElementManagementItinerary().getSegmentName();
                    if(segType.equalsIgnoreCase("AIR")) {
                        String segments = itineraryInfo.getTravelProduct().getBoardpointDetail().getCityCode() + itineraryInfo.getTravelProduct().getOffpointDetail().getCityCode();
                        if (segments.equals(key)) {
                            refDetails = new ReferencingDetailsTypeI142222C();
                            refDetails.setRefQualifier("S");
                            refDetails.setRefNumber(itineraryInfo.getElementManagementItinerary().getReference().getNumber());
                            paxSegReference.getRefDetails().add(refDetails);
                        }
                    }
                }
            }
        }

        pricepnr.setPaxSegReference(paxSegReference);


        CodedAttributeInformationType attributeDetails=new CodedAttributeInformationType();
        List<CodedAttributeInformationType> attributeList = new ArrayList<>();
        //attributeDetails.setAttributeType("BK");
        //attributeDetails.setAttributeType("NOP");
        //attributeDetails.setAttributeDescription("XN");
        if(isSeamen) {
            if(carrierCode!=null && !carrierCode.equalsIgnoreCase(airlineStr)) {
                attributeDetails.setAttributeType("ptc");
                //overrideInformation.getAttributeDetails().add(attributeDetails);
                attributeList.add(attributeDetails);
            }
            attributeDetails=new CodedAttributeInformationType();
            attributeDetails.setAttributeType("RP");
            attributeList.add(attributeDetails);
            attributeDetails=new CodedAttributeInformationType();
            attributeDetails.setAttributeType("RU");
            attributeList.add(attributeDetails);

            if(carrierCode!=null && carrierCode.equalsIgnoreCase(airlineStr)) {
                attributeDetails=new CodedAttributeInformationType();
                attributeDetails.setAttributeType("RW");
                attributeDetails.setAttributeDescription("029608");
                attributeList.add(attributeDetails);

            } else {
                attributeDetails=new CodedAttributeInformationType();
                attributeDetails.setAttributeType("RLO");
                attributeList.add(attributeDetails);
            }
            overrideInformation.getAttributeDetails().addAll(attributeList);


        }else {
            overrideInformation.getAttributeDetails().addAll(addPricingOptions(isDomesticFlight));
        }



        pricepnr.setOverrideInformation(overrideInformation);

        TransportIdentifierType validatingCarrier = new TransportIdentifierType();
        CompanyIdentificationTypeI carrierInformation = new CompanyIdentificationTypeI();

        carrierInformation.setCarrierCode(carrierCode);
        validatingCarrier.setCarrierInformation(carrierInformation);
        pricepnr.setValidatingCarrier(validatingCarrier);

        if(isDomesticFlight && flightItinerary.getPricingInformation(isSeamen).getPaxFareDetailsList() != null && !flightItinerary.getPricingInformation(isSeamen).getPaxFareDetailsList().isEmpty()){
            List<FareJourney> fareJourneys = flightItinerary.getPricingInformation(isSeamen).getPaxFareDetailsList().get(0).getFareJourneyList();
            for(FareJourney fareJourney : fareJourneys){
                FarePricePNRWithBookingClass.PricingFareBase pricingFareBase = new FarePricePNRWithBookingClass.PricingFareBase();
                FareQualifierDetailsTypeI fareBasisOptions = new FareQualifierDetailsTypeI();
                AdditionalFareQualifierDetailsTypeI fareBasisDetails = new AdditionalFareQualifierDetailsTypeI();
                String fareBasis = fareJourney.getFareSegmentList().get(0).getFareBasis();
                String primaryCode = fareBasis.substring(0, 3);
                fareBasisDetails.setPrimaryCode(primaryCode);
                if(fareBasis.length() > 3){
                    String basisCode = fareBasis.substring(3);
                    fareBasisDetails.setFareBasisCode(basisCode);
                }
                fareBasisOptions.setFareBasisDetails(fareBasisDetails);
                /*pricingFareBase.setFareBasisOptions(fareBasisOptions);

                ReferenceInformationTypeI94606S fareBasisSegReferenc = new ReferenceInformationTypeI94606S();
                ReferencingDetailsTypeI142223C referencingDetails = new ReferencingDetailsTypeI142223C();
                referencingDetails.setRefNumber(BigInteger.valueOf(journeyIndex));
                referencingDetails.setRefQualifier("S");
                fareBasisSegReferenc.getRefDetails().add(referencingDetails);
                pricingFareBase.setFareBasisSegReference(fareBasisSegReferenc);

                journeyIndex = journeyIndex + 1;
                pricepnr.getPricingFareBase().add(pricingFareBase);*/
            }
        }
        if(isSeamen) {
            FarePricePNRWithBookingClass.DiscountInformation discountInfo = new FarePricePNRWithBookingClass.DiscountInformation();
            DiscountAndPenaltyInformationTypeI penDisInfo = new DiscountAndPenaltyInformationTypeI();
            penDisInfo.setInfoQualifier("701");
            DiscountPenaltyMonetaryInformationTypeI penDisData = new DiscountPenaltyMonetaryInformationTypeI();
            if(carrierCode.equalsIgnoreCase("UK")) {
                penDisData.setDiscountCode("SC");
            } else {
                penDisData.setDiscountCode("SEA");
            }
            penDisInfo.getPenDisData().add(penDisData);
            discountInfo.setPenDisInformation(penDisInfo);
            pricepnr.getDiscountInformation().add(discountInfo);
        }


         // AddBooking scenario: original booking was Seamen fare, and current AddBooking is Corporate fare
        if (isAddBooking){
           if (!isSeamen) {
               boolean isOriginalBookingSeamen = isOriginalBookingSeamen(pnrReply);
               if (isOriginalBookingSeamen) {
                   addChildDiscountIfApplicable(pnrReply, carrierCode, paxSegReference, pricepnr);
               }
           }
        }

        return pricepnr;
    }


    public List<CodedAttributeInformationType> addPricingOptions(boolean isDomestic){
        List<CodedAttributeInformationType> attributeList = new ArrayList<>();
        CodedAttributeInformationType codedAttributeInformationType = new CodedAttributeInformationType();
        codedAttributeInformationType.setAttributeType("RP");
        attributeList.add(codedAttributeInformationType);
        codedAttributeInformationType = new CodedAttributeInformationType();
        codedAttributeInformationType.setAttributeType("RU");
        attributeList.add(codedAttributeInformationType);

        codedAttributeInformationType = new CodedAttributeInformationType();
        if (isDomestic) {
            codedAttributeInformationType.setAttributeType("FBA");
        }else {
            codedAttributeInformationType.setAttributeType("RLO");
        }
        attributeList.add(codedAttributeInformationType);


        return attributeList;
    }
    
    public void helper(){
        FarePricePNRWithBookingClass pricepnr=new FarePricePNRWithBookingClass();

        ConversionRateTypeI currencyOverride = new ConversionRateTypeI();
        ConversionRateDetailsTypeI firstRateDetail=new ConversionRateDetailsTypeI();
        firstRateDetail.setCurrencyCode("INR");
        currencyOverride.setFirstRateDetail(firstRateDetail);
        CodedAttributeType overrideInformation=new CodedAttributeType();
        CodedAttributeInformationType attributeDetails=new CodedAttributeInformationType();

        attributeDetails.setAttributeType("AC");

        overrideInformation.getAttributeDetails().add(attributeDetails);
        pricepnr.setOverrideInformation(overrideInformation);
        pricepnr.setCurrencyOverride(currencyOverride);
    }

    public FarePricePNRWithBookingClass lpf(){
        FarePricePNRWithBookingClass pricepnr = new FarePricePNRWithBookingClass();
        CodedAttributeType overrideInformation = new CodedAttributeType();
        CodedAttributeInformationType attributeDetails = new CodedAttributeInformationType();
        attributeDetails.setAttributeType("RLO");
        overrideInformation.getAttributeDetails().add(attributeDetails);
        pricepnr.setOverrideInformation(overrideInformation);

        return  pricepnr;

    }



    // Check if the original PNR booking was made under Seamen fare type
    private boolean isOriginalBookingSeamen(PNRReply pnrReply) {
        if (pnrReply == null || pnrReply.getTravellerInfo() == null) {
            return false;
        }

        for (PNRReply.TravellerInfo travellerInfo : pnrReply.getTravellerInfo()) {
            if (travellerInfo.getPassengerData() != null) {
                for (PNRReply.TravellerInfo.PassengerData passengerData : travellerInfo.getPassengerData()) {
                    if (passengerData.getTravellerInformation() != null &&
                            passengerData.getTravellerInformation().getPassenger() != null &&
                            !passengerData.getTravellerInformation().getPassenger().isEmpty()) {

                        String passengerType = passengerData.getTravellerInformation()
                                .getPassenger().get(0).getType();

                        if ("SEA".equalsIgnoreCase(passengerType) || "SC".equalsIgnoreCase(passengerType)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


//      Adds child discount if child passengers exist
    private void addChildDiscountIfApplicable(PNRReply pnrReply, String carrierCode, ReferenceInformationTypeI94605S paxSegReference, FarePricePNRWithBookingClass pricepnr) {

        if (pnrReply == null || pnrReply.getTravellerInfo() == null) {
            return;
        }

        // Collect all child passenger reference numbers
        List<BigInteger> childPassengerRefs = new ArrayList<>();

        for (PNRReply.TravellerInfo travellerInfo : pnrReply.getTravellerInfo()) {
            if (travellerInfo.getElementManagementPassenger() != null &&
                    travellerInfo.getElementManagementPassenger().getReference() != null &&
                    travellerInfo.getPassengerData() != null &&
                    !travellerInfo.getPassengerData().isEmpty()) {

                for (PNRReply.TravellerInfo.PassengerData passengerData : travellerInfo.getPassengerData()) {
                    if (passengerData.getTravellerInformation() != null &&
                            passengerData.getTravellerInformation().getPassenger() != null &&
                            !passengerData.getTravellerInformation().getPassenger().isEmpty()) {

                        String firstName = passengerData.getTravellerInformation()
                                .getPassenger().get(0).getFirstName();

                        if (firstName != null) {
                            String[] nameParts = firstName.trim().split("\\s+");
                            if (nameParts.length > 1) {
                                String suffix = nameParts[nameParts.length - 1].toUpperCase();

                                // Detect child name suffixes (MSTR, MISS, MASTER)
                                if (suffix.equals("MSTR") || suffix.equals("MISS") || suffix.equals("MASTER")) {
                                    BigInteger passengerRef = travellerInfo.getElementManagementPassenger()
                                            .getReference().getNumber();
                                    if (passengerRef != null) {
                                        childPassengerRefs.add(passengerRef);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (childPassengerRefs.isEmpty()) {
            return;
        }

        //   discount  for each child passenger
        for (BigInteger childRef : childPassengerRefs) {
            FarePricePNRWithBookingClass.DiscountInformation discountInfo = new FarePricePNRWithBookingClass.DiscountInformation();

            DiscountAndPenaltyInformationTypeI penDisInfo = new DiscountAndPenaltyInformationTypeI();
            penDisInfo.setInfoQualifier("701");

            DiscountPenaltyMonetaryInformationTypeI penDisData = new DiscountPenaltyMonetaryInformationTypeI();
            penDisData.setDiscountCode("CH");
            penDisInfo.getPenDisData().add(penDisData);

            discountInfo.setPenDisInformation(penDisInfo);

            ReferenceInformationTypeI94606S referenceQualifier = new ReferenceInformationTypeI94606S();

            ReferencingDetailsTypeI142223C refChildP = new ReferencingDetailsTypeI142223C();
            refChildP.setRefQualifier("P");
            refChildP.setRefNumber(childRef);
            referenceQualifier.getRefDetails().add(refChildP);

            if (paxSegReference != null && paxSegReference.getRefDetails() != null) {
                for (ReferencingDetailsTypeI142222C segRef : paxSegReference.getRefDetails()) {
                    if ("S".equals(segRef.getRefQualifier())) {
                        ReferencingDetailsTypeI142223C refChildS = new ReferencingDetailsTypeI142223C();
                        refChildS.setRefQualifier("S");
                        refChildS.setRefNumber(segRef.getRefNumber());
                        referenceQualifier.getRefDetails().add(refChildS);
                    }
                }
            }

            discountInfo.setReferenceQualifier(referenceQualifier);

            pricepnr.getDiscountInformation().add(discountInfo);
        }
    }

}
