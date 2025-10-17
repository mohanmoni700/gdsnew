package com.compassites.GDSWrapper.amadeus;

import com.amadeus.xml.satrqt_19_1_1a.*;
import com.compassites.model.AirSegmentInformation;
import com.compassites.model.FlightItinerary;
import com.compassites.model.Journey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UpsellServicesReq {

    private static final Logger logger = LoggerFactory.getLogger("gds");

    public AirMultiAvailability getAvailableRbdUpsell(FlightItinerary flightItinerary, boolean isSeamen) {

        AirMultiAvailability airMultiAvailability = new AirMultiAvailability();

        try {

            MessageActionDetailsType messageActionDetails = new MessageActionDetailsType();
            MessageFunctionBusinessDetailsType functionDetails = new MessageFunctionBusinessDetailsType();
            functionDetails.setActionCode("44");
            messageActionDetails.setFunctionDetails(functionDetails);

            List<AirMultiAvailability.RequestSection> requestSectionList = new ArrayList<>();

            List<Journey> journeyList = isSeamen ? flightItinerary.getJourneyList() : flightItinerary.getNonSeamenJourneyList();

            if (journeyList != null && !journeyList.isEmpty()) {
                for (Journey journey : journeyList) {

                    AvailabilityProductionInfoType availabilityProductInfo = new AvailabilityProductionInfoType();
                    List<AirlineOptionType> airlineOrFlightOptionList = new ArrayList<>();

                    List<AirSegmentInformation> airSegmentList = journey.getAirSegmentList();

                    List<AirMultiAvailability.RequestSection.QualifiedConnectionOption> qualifiedConnectionOptionList = null;
                    if (airSegmentList != null && !airSegmentList.isEmpty()) {

                        // Get origin and destination from first and last segments
                        AirSegmentInformation firstSegment = airSegmentList.get(0);
                        AirSegmentInformation lastSegment = airSegmentList.get(airSegmentList.size() - 1);

                        // Setting availabilityProductInfo with origin details
                        ProductDateTimeType availabilityDetails = new ProductDateTimeType();
                        availabilityDetails.setDepartureDate(firstSegment.getFromDate());
                        availabilityDetails.setDepartureTime(OffsetDateTime.parse(firstSegment.getDepartureTime()).format(DateTimeFormatter.ofPattern("HHmm")));
                        availabilityProductInfo.getAvailabilityDetails().add(availabilityDetails);

                        // Set departure location
                        LocationDetailsType departureLocationInfo = new LocationDetailsType();
                        departureLocationInfo.setCityAirport(firstSegment.getFromLocation());
                        availabilityProductInfo.setDepartureLocationInfo(departureLocationInfo);

                        // Set arrival location
                        LocationDetailsType arrivalLocationInfo = new LocationDetailsType();
                        arrivalLocationInfo.setCityAirport(lastSegment.getToLocation());
                        availabilityProductInfo.setArrivalLocationInfo(arrivalLocationInfo);

                        // Handle connecting flights if more than one segment
                        if (airSegmentList.size() > 1) {
                            qualifiedConnectionOptionList = new ArrayList<>();
                            AirMultiAvailability.RequestSection.QualifiedConnectionOption qualifiedConnectionOption = new AirMultiAvailability.RequestSection.QualifiedConnectionOption();
                            ConnectionType connectionOption = new ConnectionType();

                            // Set first connection - get fromLocation of segment at index 1 (skip index 0)
                            AirSegmentInformation firstConnectionSegment = airSegmentList.get(1);
                            ConnectPointDetailsType firstConnection = new ConnectPointDetailsType();
                            firstConnection.setLocation(firstConnectionSegment.getFromLocation());
//                            firstConnection.setTime(OffsetDateTime.parse(firstConnectionSegment.getDepartureTime()).format(DateTimeFormatter.ofPattern("HHmm")));
                            connectionOption.setFirstConnection(firstConnection);

                            // Set second connections (if more than 2 segments) - get fromLocation of remaining segments
                            if (airSegmentList.size() > 2) {
                                List<ConnectPointDetailsType> secondConnectionList = new ArrayList<>();
                                for (int i = 2; i < airSegmentList.size(); i++) {
                                    AirSegmentInformation connectionSegment = airSegmentList.get(i);
                                    ConnectPointDetailsType secondConnection = new ConnectPointDetailsType();
                                    secondConnection.setLocation(connectionSegment.getFromLocation());
//                                    secondConnection.setTime(OffsetDateTime.parse(connectionSegment.getDepartureTime()).format(DateTimeFormatter.ofPattern("HHmm")));
                                    secondConnectionList.add(secondConnection);
                                }
                                connectionOption.getSecondConnection().addAll(secondConnectionList);
                            }

                            qualifiedConnectionOption.setConnectionOption(connectionOption);
                            qualifiedConnectionOptionList.add(qualifiedConnectionOption);
                        }

                        // Setting flight details for each segment
                        AirlineOptionType airlineOption = new AirlineOptionType();
                        for (AirSegmentInformation airSegment : airSegmentList) {
                            FullFlightIdentificationType flightIdentification = new FullFlightIdentificationType();
                            flightIdentification.setAirlineCode(airSegment.getCarrierCode());
                            flightIdentification.setNumber(airSegment.getFlightNumber());

                            airlineOption.getFlightIdentification().add(flightIdentification);
                        }
                        airlineOrFlightOptionList.add(airlineOption);
                    }

                    // set OrderClassesByCabin as 702 to get rbd grouped based on cabin class
                    CabinDescriptionType cabinOption = new CabinDescriptionType();
                    cabinOption.setOrderClassesByCabin("702");

                    // Setting availability Options
                    AvailabilityOptionsType availabilityOptions = new AvailabilityOptionsType();
                    availabilityOptions.setTypeOfRequest("TN");

                    // Setting optionClass (commented out as per original code)
                    // ClassQueryType optionClass = new ClassQueryType();
                    // ProductDetailsType productDetails = new ProductDetailsType();
                    // productDetails.setServiceClass(rbdUpsellRequest.getCabinClass());
                    // optionClass.getProductClassDetails().add(productDetails);

                    // Populating RequestSection
                    AirMultiAvailability.RequestSection requestSection = new AirMultiAvailability.RequestSection();
                    requestSection.setAvailabilityProductInfo(availabilityProductInfo);
                    // requestSection.setOptionClass(optionClass);
                    requestSection.setAvailabilityOptions(availabilityOptions);
                    requestSection.getAirlineOrFlightOption().addAll(airlineOrFlightOptionList);
                    requestSection.setCabinOption(cabinOption);

                    // Set qualified connection options in RequestSection if there are connectingPoints
                    if (qualifiedConnectionOptionList != null && !qualifiedConnectionOptionList.isEmpty()) {
                        requestSection.getQualifiedConnectionOption().addAll(qualifiedConnectionOptionList);
                    }
                    requestSectionList.add(requestSection);
                }
            }
            airMultiAvailability.setMessageActionDetails(messageActionDetails);
            airMultiAvailability.getRequestSection().addAll(requestSectionList);

        } catch (Exception e) {
            logger.debug("Error with AirMultiAvailability request creation: {}", e.getMessage(), e);
        }
        return airMultiAvailability;
    }
}