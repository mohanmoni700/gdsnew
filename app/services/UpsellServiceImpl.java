package services;

import com.amadeus.xml.satrsp_19_1_1a.AirMultiAvailabilityReply;
import com.amadeus.xml.satrsp_19_1_1a.CabinClassDesignationType;
import com.amadeus.xml.satrsp_19_1_1a.CabinDetailsType;
import com.amadeus.xml.satrsp_19_1_1a.ClassInformationType;
import com.amadeus.xml.satrsp_19_1_1a.ProductDetailsType;
import com.amadeus.xml.tipnrr_13_2_1a.*;
import com.compassites.GDSWrapper.amadeus.ServiceHandler;
import com.compassites.constants.AmadeusConstants;
import com.compassites.model.*;
import dto.CabinDetails;
import dto.Upsell.*;
import models.AmadeusSessionWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UpsellServiceImpl implements UpsellService{

    private static final Logger logger = LoggerFactory.getLogger("gds");

    private static final Map<String, BigInteger> cabinClassMap = new HashMap<>();

    private static final Map<BigInteger, String> reverseCabinClassMap = new HashMap<>();


    static {
        cabinClassMap.put("F", BigInteger.valueOf(1));  // First class
        cabinClassMap.put("C", BigInteger.valueOf(2));  // Business class
        cabinClassMap.put("M", BigInteger.valueOf(3));  // Economy Standard
        cabinClassMap.put("Y", BigInteger.valueOf(3));  // Economy
        cabinClassMap.put("W", BigInteger.valueOf(4));  // Premium Economy

        for (Map.Entry<String, BigInteger> e : cabinClassMap.entrySet()) {
            reverseCabinClassMap.put(e.getValue(), e.getKey());
        }

    }


    @Override
//  Retrieves RBD  upsell availability for flight segments.
    public Map<String, Map<String, List<String>>> getRbdUpsellAvailability(RbdUpsellReqDto upsellRequest) {

        ServiceHandler serviceHandler = null;
        AmadeusSessionWrapper amadeusSessionWrapper = null;
        Map<String, Map<String, List<String>>> finalMap = new LinkedHashMap<>();

        try {
            serviceHandler = new ServiceHandler();
            String officeId = upsellRequest.getOfficeId();
            amadeusSessionWrapper = serviceHandler.logIn(officeId, true);

            FlightItinerary flightItinerary = upsellRequest.getFlightItinerary();
            boolean isSeamen = upsellRequest.isSeamen();
            List<String> requestedCabinClasses = upsellRequest.getCabinClass();

            AirMultiAvailabilityReply airMultiAvailabilityReply = serviceHandler.getAirMultiAvailability(amadeusSessionWrapper, flightItinerary, isSeamen);

            if (airMultiAvailabilityReply == null) {
                logger.warn("Received null availability reply from Amadeus for officeId: {}", officeId);
                return finalMap;
            }

            List<AirMultiAvailabilityReply.SingleCityPairInfo> singleCityPairInfo = airMultiAvailabilityReply.getSingleCityPairInfo();

            if (singleCityPairInfo != null && !singleCityPairInfo.isEmpty()) {

                // Process each city pair (journey)
                for (AirMultiAvailabilityReply.SingleCityPairInfo singleCityPair : singleCityPairInfo) {

                    String journeyOrigin = singleCityPair.getLocationDetails().getOrigin();
                    String journeyDestination = singleCityPair.getLocationDetails().getDestination();
                    String journeyKey = journeyOrigin + "-" + journeyDestination;

                    Map<String, List<String>> segmentRbdMap = new LinkedHashMap<>();

                    List<AirMultiAvailabilityReply.SingleCityPairInfo.FlightInfo> flightInfos = singleCityPair.getFlightInfo();


                    if (flightInfos == null || flightInfos.isEmpty()) {
                        logger.warn("No flight segments found for journey {}-{} in officeId: {}",
                                journeyOrigin, journeyDestination, officeId);
                        return null;
                    }

                    if (flightInfos != null && !flightInfos.isEmpty()) {

                        // Process each flight segment
                        for (AirMultiAvailabilityReply.SingleCityPairInfo.FlightInfo flightInfo : flightInfos) {

                            String legOrigin = flightInfo.getBasicFlightInfo().getDepartureLocation().getCityAirport();
                            String legDestination = flightInfo.getBasicFlightInfo().getArrivalLocation().getCityAirport();
                            String legKey = legOrigin + "-" + legDestination;

                            List<String> availabilityList = segmentRbdMap.getOrDefault(legKey, new ArrayList<>());

                            List<AirMultiAvailabilityReply.SingleCityPairInfo.FlightInfo.CabinClassInfo> cabinClassInfoList = flightInfo.getCabinClassInfo();

                            if (cabinClassInfoList != null && !cabinClassInfoList.isEmpty()) {
                                for (AirMultiAvailabilityReply.SingleCityPairInfo.FlightInfo.CabinClassInfo cabinClassInfo : cabinClassInfoList) {
                                    CabinDetailsType cabinInfo = cabinClassInfo.getCabinInfo();

                                    if (cabinInfo != null && cabinInfo.getCabinDesignation() != null) {
                                        CabinClassDesignationType cabinDesignation = cabinInfo.getCabinDesignation();
                                        BigInteger cabinClassOfService = cabinDesignation.getCabinClassOfService();
                                        String cabinLetter = reverseCabinClassMap.getOrDefault(cabinClassOfService, "");

                                        // Filter by requested cabin classes if specified
                                        if (requestedCabinClasses != null && !requestedCabinClasses.isEmpty()
                                                && requestedCabinClasses.stream().noneMatch(c -> c.equalsIgnoreCase(cabinLetter))) {
                                            continue;
                                        }

                                        List<ClassInformationType> infoByCabinOnClasses = cabinClassInfo.getInfoByCabinOnClasses();
                                        if (infoByCabinOnClasses != null && !infoByCabinOnClasses.isEmpty()) {
                                            for (ClassInformationType classInfo : infoByCabinOnClasses) {
                                                ProductDetailsType productClassDetail = classInfo.getProductClassDetail();

                                                if (productClassDetail != null) {
                                                    String serviceClass = productClassDetail.getServiceClass();
                                                    String availabilityStatus = productClassDetail.getAvailabilityStatus();

                                                    // Build RBD string with service class and availability count
                                                    if (serviceClass != null && StringUtils.isNumeric(availabilityStatus) && !availabilityStatus.isEmpty()) {
//                                                        String rbd = cabinLetter + "-" + serviceClass + availabilityStatus;
                                                        String rbd = serviceClass + availabilityStatus;
                                                        if (!availabilityList.contains(rbd)) {
                                                            availabilityList.add(rbd);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Add segment RBDs to map if any were found
                            if (!availabilityList.isEmpty()) {
                                segmentRbdMap.put(legKey, availabilityList);
                            }
                        }
                    }

                    // Add journey to final map if it has segments with RBDs
                    if (!segmentRbdMap.isEmpty()) {
                        finalMap.put(journeyKey, segmentRbdMap);
                    }
                }
            }

            logger.info("Successfully retrieved RBD upsell availability for {} journeys", finalMap.size());

        } catch (Exception e) {
            logger.error("Error fetching RBD upsell availability. OfficeId: {}, IsSeamen: {}",
                    upsellRequest.getOfficeId(), upsellRequest.isSeamen(), e);
            return new LinkedHashMap<>(); // Return empty map instead of null
        } finally {
            // Ensure Amadeus session is closed
            if (amadeusSessionWrapper != null && serviceHandler != null) {
                try {
                    serviceHandler.logOut(amadeusSessionWrapper);
                } catch (Exception e) {
                    logger.warn("Failed to logout from Amadeus session", e);
                }
            }
        }

        return finalMap;
    }


    public RbdUpgradePriceResponse getRbdUpgradePriceDetails(PriceRbdUpsellDto requestDto) {

        ServiceHandler serviceHandler = null;
        AmadeusSessionWrapper amadeusSessionWrapper = null;
        RbdUpgradePriceResponse rbdUpgradePriceResponse = new RbdUpgradePriceResponse();

        try {
            serviceHandler = new ServiceHandler();
            
            amadeusSessionWrapper = serviceHandler.logIn(requestDto.getOfficeId(), true);

            List<Journey> journeyList = requestDto.getJourneyList();
            PricingInformation pricingInformation = requestDto.getPricingInformation();
            List<PAXFareDetails> paxFareDetailsList = pricingInformation.getPaxFareDetailsList();
            SelectedRBDDetails selectedRBDDetails = requestDto.getSelectedRBDDetails();

            // Filter journey list to include only segments matching the selected RBD
            List<Journey> updatedJourneyList = updateJourneyList(journeyList,selectedRBDDetails);


//            update the paxFareDetailsList by selectedRBD Details
            List<PAXFareDetails> paxFareDetails = updatePaxFareDetailsWithSelectedRbd(paxFareDetailsList, selectedRBDDetails);

            com.amadeus.xml.tipnrr_13_2_1a.FareInformativePricingWithoutPNRReply fareInformativePricingWithoutPNRReply = serviceHandler.getFareInfo_32(updatedJourneyList, requestDto.isSeamen(), requestDto.getAdultCount(), requestDto.getChildCount(), requestDto.getInfantCount(), paxFareDetails, amadeusSessionWrapper, true);

            if (fareInformativePricingWithoutPNRReply.getErrorGroup() != null) {

                String errorCode = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorOrWarningCodeDetails().getErrorDetails().getErrorCode();
                String errorCategory = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorOrWarningCodeDetails().getErrorDetails().getErrorCategory();
                List<String> errorTextList = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorWarningDescription().getFreeText();
                String errorText = (errorTextList != null && !errorTextList.isEmpty())
                        ? errorTextList.get(0) : "Unknown error occurred while fetching RBD upgrade price.";


                logger.error("Amadeus fare info error for booking class [{}]: Code={}, Category={}, Message={}",
                        selectedRBDDetails.getBookingClass(), errorCode, errorCategory, errorText);

                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setErrorCode(errorCode);
                errorMessage.setMessage(errorText);
                errorMessage.setType(ErrorMessage.ErrorType.ERROR);
                rbdUpgradePriceResponse.setErrorMessage(errorMessage);
                rbdUpgradePriceResponse.setStatus("ERROR");
                return rbdUpgradePriceResponse;

            }

            List<com.amadeus.xml.tipnrr_13_2_1a.FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup> pricingGroupLevelGroup = fareInformativePricingWithoutPNRReply.getMainGroup().getPricingGroupLevelGroup();

            List<PassengerPricingDetail>  passengerPricingDetails = getPricingDetailsforSelectedRbd(pricingGroupLevelGroup);

            rbdUpgradePriceResponse = getRBDPriceResponse(passengerPricingDetails, selectedRBDDetails, pricingInformation, requestDto.isSeamen(), paxFareDetails, requestDto.getAdultCount(), requestDto.getChildCount(), requestDto.getInfantCount());

            rbdUpgradePriceResponse.setStatus("SUCCESS");

        } catch (Exception e) {
            logger.error("Error inside the getRbdUpgradePriceDetails ",e );
           throw new RuntimeException();

        }finally {
            if (amadeusSessionWrapper != null && serviceHandler != null) {
                try {
                    serviceHandler.logOut(amadeusSessionWrapper);
                } catch (Exception e) {
                    logger.warn("Failed to logout from Amadeus session", e);
                }
            }
        }

        return  rbdUpgradePriceResponse;
    }


//     Filters the journey list to retain only airsegments that match the specified criteria.
    private List<Journey> updateJourneyList(List<Journey> journeyList, SelectedRBDDetails selectedRBDDetails) {
        try {
            // Extract filter criteria from selected RBD details
            String segmentOrigin = selectedRBDDetails.getSegmentOrigin();
            String segmentDestination = selectedRBDDetails.getSegmentDestination();
            String departureTime = selectedRBDDetails.getDepartureTime();

            // Iterate through each journey to filter air segments
            for (Journey journey : journeyList) {
                List<AirSegmentInformation> filteredSegments = new ArrayList<>();

                for (AirSegmentInformation airSegmentInformation : journey.getAirSegmentList()) {

                    // Check if the segment matches all criteria
                    if (airSegmentInformation.getFromLocation().equalsIgnoreCase(segmentOrigin)
                            && airSegmentInformation.getToLocation().equalsIgnoreCase(segmentDestination)
                            && airSegmentInformation.getDepartureTime().equalsIgnoreCase(departureTime)) {

                        filteredSegments.add(airSegmentInformation);
                    }
                }

                // Update the journey with only matching Airsegments
                journey.setAirSegmentList(filteredSegments);
            }

            return journeyList;

        } catch (RuntimeException e) {
            logger.error("Error occurred while filtering journey list. Origin: {}, Destination: {}, DepartureTime: {}",
                    selectedRBDDetails.getSegmentOrigin(), selectedRBDDetails.getSegmentDestination(), selectedRBDDetails.getDepartureTime(), e);
            throw new RuntimeException("Failed to update journey list with selected RBD details", e);
        }
    }

    private RbdUpgradePriceResponse getRBDPriceResponse(List<PassengerPricingDetail> passengerPricingDetails, SelectedRBDDetails selectedRBDDetails, PricingInformation pricingInformation,
            boolean seamen, List<PAXFareDetails> paxFareDetails, int adultCount, int childCount, int infCount) {

        try {
            RbdUpgradePriceResponse rbdUpgradePriceResponse = new RbdUpgradePriceResponse();

            BigDecimal adtTax = BigDecimal.ZERO;
            BigDecimal chdTax = BigDecimal.ZERO;
            BigDecimal infTax = BigDecimal.ZERO;

            for (PassengerPricingDetail pricingDetails : passengerPricingDetails) {
                String paxType = pricingDetails.getPassengerType();

                if (paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) {
                    pricingInformation.setAdtBasePrice(pricingDetails.getBaseFare());
                    pricingInformation.setAdtTotalPrice(pricingDetails.getTotalFare());
                    adtTax = pricingInformation.getAdtTotalPrice().subtract(pricingInformation.getAdtBasePrice());
                }

                if (paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) {
                    pricingInformation.setChdBasePrice(pricingDetails.getBaseFare());
                    pricingInformation.setChdTotalPrice(pricingDetails.getTotalFare());
                    chdTax = pricingInformation.getChdTotalPrice().subtract(pricingInformation.getChdBasePrice());
                }

                if (paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) {
                    pricingInformation.setInfBasePrice(pricingDetails.getBaseFare());
                    pricingInformation.setInfTotalPrice(pricingDetails.getTotalFare());
                    infTax = pricingInformation.getInfTotalPrice().subtract(pricingInformation.getInfBasePrice());
                }
            }

            // Update cabin details
            List<CabinDetails> existingCabinDetails = pricingInformation.getCabinDetails();
            if (existingCabinDetails != null && !existingCabinDetails.isEmpty()) {
                for (CabinDetails cabinDetail : existingCabinDetails) {
                    cabinDetail.setRbd(selectedRBDDetails.getBookingClass());
                    cabinDetail.setCabin(selectedRBDDetails.getCabinClass());
                    cabinDetail.setAvailableSeats(String.valueOf(selectedRBDDetails.getAvailableSeats()));
                }
            }

            //  Update passenger taxes
            for (PassengerTax passengerTax : pricingInformation.getPassengerTaxes()) {
                String paxType = passengerTax.getPassengerType();
                if ((paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) && adtTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(adtTax);
                }
                if ((paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) && chdTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(chdTax);
                }
                if ((paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) && infTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(infTax);
                }
            }

            //  Update paxFareDetails

                for (PAXFareDetails paxFareDetail : paxFareDetails) {
                    PassengerTypeCode paxType = paxFareDetail.getPassengerTypeCode();

                        for (FareJourney fareJourney : paxFareDetail.getFareJourneyList()) {
                            for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {
                                fareSegment.setAvailableSeats(String.valueOf(selectedRBDDetails.getAvailableSeats()));
                                fareSegment.setBookingClass(selectedRBDDetails.getBookingClass());
                                fareSegment.setCabinClass(selectedRBDDetails.getCabinClass());

                                // set FareBasis for all passenger types
                                for (PassengerPricingDetail pricingDetails : passengerPricingDetails) {
                                    PassengerTypeCode passengerTypeCode = pricingDetails.getPassengerTypeCode();
                                    PassengerTypeCode passengerType = PassengerPricingDetail.updatePassengerTypeCode(passengerTypeCode, seamen);
                                    if (paxType == passengerType) {
                                        fareSegment.setFareBasis(pricingDetails.getFareBasis());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                pricingInformation.setPaxFareDetailsList(paxFareDetails);

//             Calculate total base price
            BigDecimal totalBasePrice = zeroIfNull(pricingInformation.getAdtBasePrice()).multiply(BigDecimal.valueOf(adultCount))
                    .add(zeroIfNull(pricingInformation.getChdBasePrice()).multiply(BigDecimal.valueOf(childCount)))
                    .add(zeroIfNull(pricingInformation.getInfBasePrice()).multiply(BigDecimal.valueOf(infCount)));
            pricingInformation.setBasePrice(totalBasePrice);


//             Calculate total fare
            BigDecimal totalFare = zeroIfNull(pricingInformation.getAdtTotalPrice()).multiply(BigDecimal.valueOf(adultCount))
                    .add(zeroIfNull(pricingInformation.getChdTotalPrice()).multiply(BigDecimal.valueOf(childCount)))
                    .add(zeroIfNull(pricingInformation.getInfTotalPrice()).multiply(BigDecimal.valueOf(infCount)));
            pricingInformation.setTotalPrice(totalFare);

//             Calculate total tax
            pricingInformation.setTotalTax(totalFare.subtract(totalBasePrice));

//         Baggage and fareBasis update for Adult
            String baggageAllowance = passengerPricingDetails.get(0).getSegments().get(0).getBaggageAllowance();
            pricingInformation.getMnrSearchBaggage().setAllowedBaggage(baggageAllowance);

            String fareBasis = passengerPricingDetails.get(0).getSegments().get(0).getFareBasis();

            //  Build final response
            rbdUpgradePriceResponse.setSeamen(seamen);
            rbdUpgradePriceResponse.setPricingInformation(pricingInformation);
            rbdUpgradePriceResponse.setFare(pricingInformation.getAdtTotalPrice());
            rbdUpgradePriceResponse.setBaggage(baggageAllowance);
            rbdUpgradePriceResponse.setBookingClas(selectedRBDDetails.getBookingClass());
            rbdUpgradePriceResponse.setCabinClass(selectedRBDDetails.getCabinClass());
            rbdUpgradePriceResponse.setAvailableSeats(selectedRBDDetails.getAvailableSeats());
            rbdUpgradePriceResponse.setFareBasis(fareBasis);
            rbdUpgradePriceResponse.setSegmentOrigin(selectedRBDDetails.getSegmentOrigin());
            rbdUpgradePriceResponse.setSegmentDestination(selectedRBDDetails.getSegmentDestination());
            rbdUpgradePriceResponse.setDepartureTime(selectedRBDDetails.getDepartureTime());

            return rbdUpgradePriceResponse;

        } catch (Exception e) {
            logger.error("Error in getRBDPriceResponse", e);
            return null;
        }
    }


    public List<PAXFareDetails> updatePaxFareDetailsWithSelectedRbd(List<PAXFareDetails> paxFareDetailsList, SelectedRBDDetails selectedRbdDetails) {

        if (paxFareDetailsList == null || selectedRbdDetails == null) {
            logger.warn("Cannot update PaxFareDetails: paxFareDetailsList or selectedRbdDetails is null");
            return Collections.emptyList();
        }

        List<PAXFareDetails> updatedList = new ArrayList<>();

        try {
            for (PAXFareDetails paxFareDetails : paxFareDetailsList) {
                if (paxFareDetails.getFareJourneyList() == null) {
                    continue;
                }

                PAXFareDetails updatedPax = new PAXFareDetails();
                updatedPax.setPassengerTypeCode(paxFareDetails.getPassengerTypeCode());

                List<FareJourney> updatedJourneys = new ArrayList<>();

                for (FareJourney fareJourney : paxFareDetails.getFareJourneyList()) {
                    if (fareJourney.getFareSegmentList() == null) {
                        continue;
                    }

                    FareJourney updatedJourney = new FareJourney();
                    List<FareSegment> updatedSegments = new ArrayList<>();

                    for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {
                        String segment = fareSegment.getSegment();
                        if (segment == null || !segment.contains(":")) {
                            continue;
                        }

                        // Split into route and departure time
                        String[] parts = segment.split(":", 2);
                        String route = parts[0].trim();
                        String departureTime = parts[1].trim();

                        // Split route into origin and destination
                        String[] routeParts = route.split("-");
                        if (routeParts.length != 2) {
                            continue;
                        }
                        String origin = routeParts[0].trim();
                        String destination = routeParts[1].trim();

                        // Compare with selectedRbdDetails
                        if (origin.equalsIgnoreCase(selectedRbdDetails.getSegmentOrigin())
                                && destination.equalsIgnoreCase(selectedRbdDetails.getSegmentDestination())
                                && departureTime.equalsIgnoreCase(selectedRbdDetails.getDepartureTime())) {

                            FareSegment updatedSegment = new FareSegment();
                            updatedSegment.setCabinClass(selectedRbdDetails.getCabinClass());
                            updatedSegment.setBookingClass(selectedRbdDetails.getBookingClass());
                            updatedSegment.setAvailableSeats(String.valueOf(selectedRbdDetails.getAvailableSeats()));
                            updatedSegment.setFareBasis(null);
                            updatedSegment.setSegment(segment);

                            updatedSegments.add(updatedSegment);
                        }
                    }

                    if (!updatedSegments.isEmpty()) {
                        updatedJourney.setFareSegmentList(updatedSegments);
                        updatedJourneys.add(updatedJourney);
                    }
                }

                if (!updatedJourneys.isEmpty()) {
                    updatedPax.setFareJourneyList(updatedJourneys);
                    updatedList.add(updatedPax);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to update PaxFareDetails with RBD ", e.getMessage(), e);
            throw new RuntimeException("Failed to update PaxFareDetails", e);
        }

        return updatedList;
    }


    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    public UpdatedItineraryResponse updateItineraryForSelectedRbd(PriceRbdUpsellDto requestDto) {
//
        ServiceHandler serviceHandler = null;
        AmadeusSessionWrapper amadeusSessionWrapper = null;
        FlightItinerary updatedFlightItinerary = null;
        ErrorMessage errorMessage = null;
//
        try {
            serviceHandler = new ServiceHandler();

            amadeusSessionWrapper = serviceHandler.logIn(requestDto.getOfficeId(), true);

            FlightItinerary flightItinerary = requestDto.getFlightItinerary();
            List<PAXFareDetails> originalPaxFareDetailsList = requestDto.isSeamen() ? flightItinerary.getSeamanPricingInformation().getPaxFareDetailsList() : flightItinerary.getPricingInformation().getPaxFareDetailsList();
            List<SelectedRbdOption> selectedRbdOption = requestDto.getSelectedRbdOption();

            // Update original passenger fare details with selected RBD
            List<PAXFareDetails> paxFareDetails = updateOriginalPaxFareDetailsWithSelectedRbd(selectedRbdOption, originalPaxFareDetailsList);

            List<Journey> journeyList = requestDto.isSeamen() ? flightItinerary.getJourneyList() : flightItinerary.getNonSeamenJourneyList();

            com.amadeus.xml.tipnrr_13_2_1a.FareInformativePricingWithoutPNRReply fareInformativePricingWithoutPNRReply = serviceHandler.getFareInfo_32(journeyList, requestDto.isSeamen(), requestDto.getAdultCount(), requestDto.getChildCount(), requestDto.getInfantCount(), paxFareDetails, amadeusSessionWrapper, true);

            if (fareInformativePricingWithoutPNRReply.getErrorGroup() != null) {

                String errorCode = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorOrWarningCodeDetails().getErrorDetails().getErrorCode();
                String errorCategory = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorOrWarningCodeDetails().getErrorDetails().getErrorCategory();
                List<String> errorTextList = fareInformativePricingWithoutPNRReply.getErrorGroup().getErrorWarningDescription().getFreeText();

                String errorText = (errorTextList != null && !errorTextList.isEmpty())
                        ? errorTextList.get(0) : "Unknown error occurred while fetching selected RBD upgrade price.";


                logger.error("Amadeus fare info error  [{}]: Code={}, Category={}, Message={}", errorCode, errorCategory, errorText);

                errorMessage = new ErrorMessage();
                errorMessage.setErrorCode(errorCode);
                errorMessage.setMessage(errorText);
                errorMessage.setType(ErrorMessage.ErrorType.ERROR);
                return new UpdatedItineraryResponse(null, errorMessage, "ERROR", null, null, null, null, null, null);

            }

            List<com.amadeus.xml.tipnrr_13_2_1a.FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup> pricingGroupLevelGroup = fareInformativePricingWithoutPNRReply.getMainGroup().getPricingGroupLevelGroup();

            List<PassengerPricingDetail> passengerPricingDetails = getPricingDetailsforSelectedRbd(pricingGroupLevelGroup);

            updatedFlightItinerary = updateFlightItinerary(passengerPricingDetails, selectedRbdOption, flightItinerary, requestDto.isSeamen(), paxFareDetails, requestDto.getAdultCount(), requestDto.getChildCount(), requestDto.getInfantCount());

            List<PAXFareDetails> updatedPaxFareDetailsList = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal pricePerAdult = BigDecimal.ZERO;
            BigDecimal pricePerChild = BigDecimal.ZERO;
            BigDecimal pricePerInfant = BigDecimal.ZERO;
            List<UpsellSegmentDetail> upsellSegmentDetailList = new ArrayList<>();

            if(updatedFlightItinerary != null) {
                 PricingInformation updatedPricingInformation = requestDto.isSeamen() ? updatedFlightItinerary.getSeamanPricingInformation() : updatedFlightItinerary.getPricingInformation();
                 totalPrice = updatedPricingInformation.getTotalPrice();
                 pricePerAdult = updatedPricingInformation.getAdtTotalPrice();
                 pricePerChild = updatedPricingInformation.getChdTotalPrice();
                 pricePerInfant = updatedPricingInformation.getInfTotalPrice();
                updatedPaxFareDetailsList = updatedPricingInformation.getPaxFareDetailsList();
                
                    if(passengerPricingDetails != null){

                        for (PassengerPricingDetail detail : passengerPricingDetails) {
                            String paxType = detail.getPassengerType();

                            if (paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) {
                                 upsellSegmentDetailList = detail.getSegments();
                            }
                        }
                    }
            }

            return new UpdatedItineraryResponse(updatedFlightItinerary, null, "SUCCESS", updatedPaxFareDetailsList, pricePerAdult, pricePerChild, pricePerInfant, totalPrice, upsellSegmentDetailList);

        } catch (Exception e) {
            logger.error("Unexpected error occurred while updating itinerary for selected RBD. OfficeId: {}, IsSeamen: {}",
                    requestDto.getOfficeId(), requestDto.isSeamen(), e);

            // Create error message for unexpected exceptions
            errorMessage = new ErrorMessage();
            errorMessage.setErrorCode("SYSTEM_ERROR");
            errorMessage.setMessage("An unexpected error occurred while processing your request");
            errorMessage.setType(ErrorMessage.ErrorType.ERROR);
            return new UpdatedItineraryResponse(null, errorMessage, "ERROR", null, null, null,null, null, null);

        } finally {
            if (amadeusSessionWrapper != null && serviceHandler != null) {
                try {
                    serviceHandler.logOut(amadeusSessionWrapper);
                } catch (Exception e) {
                    logger.warn("Failed to logout from Amadeus session", e);
                }
            }

        }
    }



//    private FlightItinerary updateFlightItinerary(List<PassengerPricingDetail> passengerPricingDetails, List<SelectedRbdOption> selectedRbdOption, FlightItinerary flightItinerary,
//                                                        boolean seamen, List<PAXFareDetails> paxFareDetails, int adultCount, int childCount, int infCount) {
//
//        try {
//
//            PricingInformation pricingInformation;
//            if(seamen) {
//                pricingInformation = flightItinerary.getSeamanPricingInformation();
//            }else{
//                pricingInformation = flightItinerary.getPricingInformation();
//            }
//
//            BigDecimal adtTax = BigDecimal.ZERO;
//            BigDecimal chdTax = BigDecimal.ZERO;
//            BigDecimal infTax = BigDecimal.ZERO;
//
//            for (PassengerPricingDetail pricingDetails : passengerPricingDetails) {
//                String paxType = pricingDetails.getPassengerType();
//
//                if (paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) {
//                    pricingInformation.setAdtBasePrice(pricingDetails.getBaseFare());
//                    pricingInformation.setAdtTotalPrice(pricingDetails.getTotalFare());
//                    adtTax = pricingInformation.getAdtTotalPrice().subtract(pricingInformation.getAdtBasePrice());
//                }
//
//                if (paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) {
//                    pricingInformation.setChdBasePrice(pricingDetails.getBaseFare());
//                    pricingInformation.setChdTotalPrice(pricingDetails.getTotalFare());
//                    chdTax = pricingInformation.getChdTotalPrice().subtract(pricingInformation.getChdBasePrice());
//                }
//
//                if (paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) {
//                    pricingInformation.setInfBasePrice(pricingDetails.getBaseFare());
//                    pricingInformation.setInfTotalPrice(pricingDetails.getTotalFare());
//                    infTax = pricingInformation.getInfTotalPrice().subtract(pricingInformation.getInfBasePrice());
//                }
//            }
//
//
//
//            //  Update passenger taxes
//            for (PassengerTax passengerTax : pricingInformation.getPassengerTaxes()) {
//                String paxType = passengerTax.getPassengerType();
//                if ((paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) && adtTax.compareTo(BigDecimal.ZERO) > 0) {
//                    passengerTax.setTotalTax(adtTax);
//                }
//                if ((paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) && chdTax.compareTo(BigDecimal.ZERO) > 0) {
//                    passengerTax.setTotalTax(chdTax);
//                }
//                if ((paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) && infTax.compareTo(BigDecimal.ZERO) > 0) {
//                    passengerTax.setTotalTax(infTax);
//                }
//            }
//
//
////            update the paxfaredetials
//            for (PAXFareDetails paxFareDetail : paxFareDetails) {
//                PassengerTypeCode paxType = paxFareDetail.getPassengerTypeCode();
//                PassengerTypeCode normalizedPaxType = PassengerPricingDetail.updatePassengerTypeCode(paxType, seamen);
//
//                // Find matching pricing detail (convert enum to string)
//                PassengerPricingDetail matchingPricingDetail = passengerPricingDetails.stream()
//                        .filter(pricing -> {
//                            PassengerTypeCode pricingType = PassengerPricingDetail.updatePassengerTypeCode(
//                                    PassengerTypeCode.valueOf(pricing.getPassengerType()), seamen);
//                            return pricingType == normalizedPaxType;
//                        })
//                        .findFirst()
//                        .orElse(null);
//
//                if (matchingPricingDetail == null) {
//                    logger.warn("No matching PassengerPricingDetail found for passenger type: {}", paxType);
//                    continue;
//                }
//
//                for (FareJourney fareJourney : paxFareDetail.getFareJourneyList()) {
//                    for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {
//
//                        // Extract origin, destination, and departure timestamp from segment string
//                        String[] segmentParts = fareSegment.getSegment().split(":");
//                        String[] routeParts = segmentParts[0].split("-");
//                        String origin = routeParts[0];
//                        String destination = routeParts[1];
//                        String departureDateTime = segmentParts[1];
//
//                        // Convert departure datetime → "ddMMyy" (to match depatureTime format in pricing)
//                        String formattedDepartureDate = departureDateTime.substring(8, 10)
//                                + departureDateTime.substring(5, 7)
//                                + departureDateTime.substring(2, 4);
//
//                        // Find matching upsell segment
//                        UpsellSegmentDetail matchingSegment = matchingPricingDetail.getSegments().stream()
//                                .filter(seg ->
//                                        seg.getOrigin().equalsIgnoreCase(origin)
//                                                && seg.getDestination().equalsIgnoreCase(destination)
//                                                && seg.getDepatureTime().equalsIgnoreCase(formattedDepartureDate)
//                                )
//                                .findFirst()
//                                .orElse(null);
//
//                        if (matchingSegment != null) {
//                            fareSegment.setFareBasis(matchingSegment.getFareBasis());
//                            fareSegment.setBookingClass(matchingSegment.getBookingClass());
//                        }
//                    }
//                }
//            }
//            pricingInformation.setPaxFareDetailsList(paxFareDetails);
//
//            // Update cabin details
//            List<CabinDetails> cabinDetailsList = CabinDetails.getCabinDetailsFromPaxFareDetails(paxFareDetails);
//            pricingInformation.setCabinDetails(cabinDetailsList);
//
//
//            // Calculate total base price
//            BigDecimal totalBasePrice = zeroIfNull(pricingInformation.getAdtBasePrice()).multiply(BigDecimal.valueOf(adultCount))
//                    .add(zeroIfNull(pricingInformation.getChdBasePrice()).multiply(BigDecimal.valueOf(childCount)))
//                    .add(zeroIfNull(pricingInformation.getInfBasePrice()).multiply(BigDecimal.valueOf(infCount)));
//            pricingInformation.setBasePrice(totalBasePrice);
//
//
//            // Calculate total fare
//            BigDecimal totalFare = zeroIfNull(pricingInformation.getAdtTotalPrice()).multiply(BigDecimal.valueOf(adultCount))
//                    .add(zeroIfNull(pricingInformation.getChdTotalPrice()).multiply(BigDecimal.valueOf(childCount)))
//                    .add(zeroIfNull(pricingInformation.getInfTotalPrice()).multiply(BigDecimal.valueOf(infCount)));
//            pricingInformation.setTotalPrice(totalFare);
//
//            // Calculate total tax
//            pricingInformation.setTotalTax(totalFare.subtract(totalBasePrice));
//            pricingInformation.setTax(totalFare.subtract(totalBasePrice));
//            pricingInformation.setTotalPriceValue(totalFare);
//            pricingInformation.setTotalBasePrice(totalBasePrice);
//            pricingInformation.setTotalCalculatedValue(totalFare);
//
////         Baggage update
//            String baggageAllowance = passengerPricingDetails.get(0).getSegments().get(0).getBaggageAllowance();
//            pricingInformation.getMnrSearchBaggage().setAllowedBaggage(baggageAllowance);
//
//            if(seamen) {
//                flightItinerary.setSeamanPricingInformation(pricingInformation);
//            }else{
//                flightItinerary.setPricingInformation(pricingInformation);
//
//            }
//
////            update the farebasis in airsegment
//
//
////            Fetch segment wise ,paxtype wise farebasis
//            Map<String, Map<PassengerTypeCode, String>> segmentFareBasisMap = new HashMap<>();
//
//            for (PAXFareDetails paxFareDetail : paxFareDetails) {
//                PassengerTypeCode passengerType = paxFareDetail.getPassengerTypeCode();
//
//                for (FareJourney fareJourney : paxFareDetail.getFareJourneyList()) {
//                    for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {
//
//                        String segmentKey = fareSegment.getSegment(); // e.g., "SIN-DXB:2025-10-24T00:50:00.000+08:00"
//
//                        Map<PassengerTypeCode, String> paxMap = segmentFareBasisMap.getOrDefault(segmentKey, new HashMap<>());
//                        paxMap.put(passengerType, fareSegment.getFareBasis());
//
//                        segmentFareBasisMap.put(segmentKey, paxMap);
//                    }
//                }
//            }
//
//
//            List<Journey> journeyList;
//            if(seamen){
//                 journeyList = flightItinerary.getJourneyList();
//            }else {
//                journeyList = flightItinerary.getNonSeamenJourneyList();
//            }
//
//            for (Journey journey : journeyList) {
//                for (AirSegmentInformation airSegment : journey.getAirSegmentList()) {
//                    for (Map.Entry<String, Map<PassengerTypeCode, String>> entry : segmentFareBasisMap.entrySet()) {
//                        String segment = entry.getKey();
//
//                        int colonIndex = segment.indexOf(':');
//                        if (colonIndex == -1) continue;
//
//                        String routePart = segment.substring(0, colonIndex);
//                        String depTime = segment.substring(colonIndex + 1);
//
//                        String[] route = routePart.split("-");
//                        if (route.length < 2) continue;
//
//                        String from = route[0];
//                        String to = route[1];
//
//                        if (airSegment.getFromLocation().equalsIgnoreCase(from)
//                                && airSegment.getToLocation().equalsIgnoreCase(to)
//                                && airSegment.getDepartureTime().equals(depTime)) {
//
//                            Map<PassengerTypeCode, String> paxFareMap = entry.getValue();
//                            String fareBasisStr = paxFareMap.entrySet().stream()
//                                    .map(e -> e.getKey().name() + ":" + e.getValue())
//                                    .collect(Collectors.joining(","));
//
////                            Set ADT, CHD, INF farebasis
////                            airSegment.setFareBasis(fareBasisStr);
//
//                            // Set only ADT  fare basis
//                            if (paxFareMap.containsKey(PassengerTypeCode.ADT)) {
//                                airSegment.setFareBasis(paxFareMap.get(PassengerTypeCode.ADT));
//                            }
//
//                            break;
//
//                        }
//                    }
//                }
//            }
//
//
//            return flightItinerary;
//
//        } catch (Exception e) {
//            logger.error("Error in getRBDPriceResponse", e);
//            return null;
//        }
//    }


private FlightItinerary updateFlightItinerary(List<PassengerPricingDetail> passengerPricingDetails, List<SelectedRbdOption> selectedRbdOption, FlightItinerary flightItinerary,
                                              boolean seamen, List<PAXFareDetails> paxFareDetails, int adultCount, int childCount, int infCount) {

    try {

        PricingInformation pricingInformation = seamen ? flightItinerary.getSeamanPricingInformation() : flightItinerary.getPricingInformation();

        //  Update Pricing, Taxes, Baggage and PaxfareDetailList
        updatePricingInformation(passengerPricingDetails, paxFareDetails, pricingInformation, seamen, adultCount, childCount, infCount);

        //  Update FareBasis in FareSegments and AirSegments
        updateFareBasisInSegments(passengerPricingDetails, paxFareDetails, flightItinerary, seamen);

        if (seamen) {
            flightItinerary.setSeamanPricingInformation(pricingInformation);
            flightItinerary.setSeamen(true);
        } else {
            flightItinerary.setPricingInformation(pricingInformation);
        }

        return flightItinerary;


    } catch (Exception e) {
        logger.error("Error in updateFlightItinerary", e);
        return null;
    }
}



//  Updates the fare basis values in the FlightItinerary's AirSegments
    private void updateFareBasisInSegments(List<PassengerPricingDetail> passengerPricingDetails, List<PAXFareDetails> paxFareDetails, FlightItinerary flightItinerary, boolean seamen) {
        try{

            //   mapping of <segmentKey, <PassengerTypeCode, FareBasis>>
            Map<String, Map<PassengerTypeCode, String>> segmentFareBasisMap = new HashMap<>();

            for (PAXFareDetails paxFareDetail : paxFareDetails) {
                PassengerTypeCode passengerType = paxFareDetail.getPassengerTypeCode();

                for (FareJourney fareJourney : paxFareDetail.getFareJourneyList()) {
                    for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {

                        String segmentKey = fareSegment.getSegment(); // "SIN-DXB:2025-10-24T00:50:00.000+08:00"

                        if (segmentKey == null || segmentKey.isEmpty()) {
                            logger.warn("Segment is null or empty for passengerType: {}, skipping", passengerType);
                            continue;
                        }

                        Map<PassengerTypeCode, String> paxMap = segmentFareBasisMap.getOrDefault(segmentKey, new HashMap<>());
                        paxMap.put(passengerType, fareSegment.getFareBasis());

                        segmentFareBasisMap.put(segmentKey, paxMap);
                    }
                }
            }


            List<Journey> journeyList;
            if(seamen){
                journeyList = flightItinerary.getJourneyList();
            }else {
                journeyList = flightItinerary.getNonSeamenJourneyList();
            }

//             Iterate through each air segment and update its fare basis

            for (Journey journey : journeyList) {
                for (AirSegmentInformation airSegment : journey.getAirSegmentList()) {
                    for (Map.Entry<String, Map<PassengerTypeCode, String>> entry : segmentFareBasisMap.entrySet()) {

                        String segment = entry.getKey();

                        int colonIndex = segment.indexOf(':');
                        if (colonIndex == -1) continue;

                        String routePart = segment.substring(0, colonIndex);
                        String depTime = segment.substring(colonIndex + 1);

                        String[] route = routePart.split("-");
                        if (route.length < 2) continue;

                        String from = route[0];
                        String to = route[1];

                        if (airSegment.getFromLocation().equalsIgnoreCase(from)
                                && airSegment.getToLocation().equalsIgnoreCase(to)
                                && airSegment.getDepartureTime().equals(depTime)) {

                            Map<PassengerTypeCode, String> paxFareMap = entry.getValue();
                            String fareBasisStr = paxFareMap.entrySet().stream()
                                    .map(e -> e.getKey().name() + ":" + e.getValue())
                                    .collect(Collectors.joining(","));

//                            Set ADT, CHD, INF farebasis
//                            airSegment.setFareBasis(fareBasisStr);

                            // Set only ADT  fare basis
                            if (paxFareMap.containsKey(PassengerTypeCode.ADT)) {
                                airSegment.setFareBasis(paxFareMap.get(PassengerTypeCode.ADT));
                            }

                            break;

                        }
                    }

                }
            }


        } catch (Exception e) {
            logger.error("Error in updateFareBasisInSegments ", e);
            throw new RuntimeException(e);
        }

    }




    private void updatePricingInformation(List<PassengerPricingDetail> passengerPricingDetails, List<PAXFareDetails> paxFareDetails, PricingInformation pricingInformation,
                                          boolean seamen, int adultCount, int childCount, int infCount) {
        try {
            BigDecimal adtTax = BigDecimal.ZERO;
            BigDecimal chdTax = BigDecimal.ZERO;
            BigDecimal infTax = BigDecimal.ZERO;

            // Update base and total prices by pax type
            for (PassengerPricingDetail detail : passengerPricingDetails) {
                String paxType = detail.getPassengerType();

                if (paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) {
                    pricingInformation.setAdtBasePrice(detail.getBaseFare());
                    pricingInformation.setAdtTotalPrice(detail.getTotalFare());
                    adtTax = detail.getTotalFare().subtract(detail.getBaseFare());

                } else if (paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) {
                    pricingInformation.setChdBasePrice(detail.getBaseFare());
                    pricingInformation.setChdTotalPrice(detail.getTotalFare());
                    chdTax = detail.getTotalFare().subtract(detail.getBaseFare());

                } else if (paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) {
                    pricingInformation.setInfBasePrice(detail.getBaseFare());
                    pricingInformation.setInfTotalPrice(detail.getTotalFare());
                    infTax = detail.getTotalFare().subtract(detail.getBaseFare());
                }
            }

            // Update passenger tax list
            for (PassengerTax passengerTax : pricingInformation.getPassengerTaxes()) {
                String paxType = passengerTax.getPassengerType();
                if ((paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA") || paxType.equalsIgnoreCase("SC")) && adtTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(adtTax);
                } else if ((paxType.equalsIgnoreCase("CHD") || paxType.equalsIgnoreCase("CH")) && chdTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(chdTax);
                } else if ((paxType.equalsIgnoreCase("INF") || paxType.equalsIgnoreCase("IN")) && infTax.compareTo(BigDecimal.ZERO) > 0) {
                    passengerTax.setTotalTax(infTax);
                }
            }


            // Calculate totals
            BigDecimal totalBasePrice = BigDecimal.ZERO;
            BigDecimal totalFare = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

//             calculate the totals for seamen
            if(seamen) {
                int paxCount = adultCount + childCount + infCount;

                totalBasePrice = zeroIfNull(pricingInformation.getAdtBasePrice()).multiply(BigDecimal.valueOf(paxCount));

                 totalFare = zeroIfNull(pricingInformation.getAdtTotalPrice()).multiply(BigDecimal.valueOf(paxCount));

                 totalTax = totalFare.subtract(totalBasePrice);

            }else{
                 totalBasePrice = zeroIfNull(pricingInformation.getAdtBasePrice()).multiply(BigDecimal.valueOf(adultCount))
                        .add(zeroIfNull(pricingInformation.getChdBasePrice()).multiply(BigDecimal.valueOf(childCount)))
                        .add(zeroIfNull(pricingInformation.getInfBasePrice()).multiply(BigDecimal.valueOf(infCount)));

                 totalFare = zeroIfNull(pricingInformation.getAdtTotalPrice()).multiply(BigDecimal.valueOf(adultCount))
                        .add(zeroIfNull(pricingInformation.getChdTotalPrice()).multiply(BigDecimal.valueOf(childCount)))
                        .add(zeroIfNull(pricingInformation.getInfTotalPrice()).multiply(BigDecimal.valueOf(infCount)));

                 totalTax = totalFare.subtract(totalBasePrice);
            }



            // Set pax fare and cabin info
            updatePaxFareDetailList(paxFareDetails, passengerPricingDetails, seamen);
            pricingInformation.setPaxFareDetailsList(paxFareDetails);
            pricingInformation.setCabinDetails(CabinDetails.getCabinDetailsFromPaxFareDetails(paxFareDetails));

            pricingInformation.setBasePrice(totalBasePrice);
            pricingInformation.setTotalPrice(totalFare);
            pricingInformation.setTax(totalTax);
            pricingInformation.setTotalTax(totalTax);
            pricingInformation.setTotalPriceValue(totalFare);
            pricingInformation.setTotalBasePrice(totalBasePrice);
            pricingInformation.setTotalCalculatedValue(totalFare);

            // Update baggage info
            String baggageAllowance = passengerPricingDetails.get(0).getSegments().get(0).getBaggageAllowance();
            pricingInformation.getMnrSearchBaggage().setAllowedBaggage(baggageAllowance);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



//    Update the fareBasis and bookingClass in paxFareDetailList
    private void updatePaxFareDetailList(List<PAXFareDetails> paxFareDetails, List<PassengerPricingDetail> passengerPricingDetails, boolean seamen) {
           try{
                for (PAXFareDetails paxFareDetail : paxFareDetails) {
                    PassengerTypeCode paxType = paxFareDetail.getPassengerTypeCode();
                    PassengerTypeCode normalizedPaxType = PassengerPricingDetail.updatePassengerTypeCode(paxType, seamen);

                    // Find matching pricing detail (convert enum to string)
                    PassengerPricingDetail matchingPricingDetail = passengerPricingDetails.stream()
                            .filter(pricing -> {
                                PassengerTypeCode pricingType = PassengerPricingDetail.updatePassengerTypeCode(
                                        PassengerTypeCode.valueOf(pricing.getPassengerType()), seamen);
                                return pricingType == normalizedPaxType;
                            })
                            .findFirst()
                            .orElse(null);

                    if (matchingPricingDetail == null) {
                        logger.warn("No matching PassengerPricingDetail found for passenger type: {}", paxType);
                        continue;
                    }

                    for (FareJourney fareJourney : paxFareDetail.getFareJourneyList()) {
                        for (FareSegment fareSegment : fareJourney.getFareSegmentList()) {

                            if (fareSegment.getSegment() == null || fareSegment.getSegment().isEmpty()) {
                                logger.warn("Segment is null or empty for paxType: {}, skipping this segment", paxType);
                                continue;
                            }

                            // Extract origin, destination, and departure timestamp from segment string
                            String[] segmentParts = fareSegment.getSegment().split(":");
                            String[] routeParts = segmentParts[0].split("-");
                            String origin = routeParts[0];
                            String destination = routeParts[1];
                            String departureDateTime = segmentParts[1];

                            // Convert departure datetime → "ddMMyy" (to match depatureTime format in pricing)
                            String formattedDepartureDate = departureDateTime.substring(8, 10)
                                    + departureDateTime.substring(5, 7)
                                    + departureDateTime.substring(2, 4);

                            // Find matching upsell segment
                            UpsellSegmentDetail matchingSegment = matchingPricingDetail.getSegments().stream()
                                    .filter(seg ->
                                            seg.getOrigin().equalsIgnoreCase(origin)
                                                    && seg.getDestination().equalsIgnoreCase(destination)
                                                    && seg.getDepatureTime().equalsIgnoreCase(formattedDepartureDate)
                                    )
                                    .findFirst()
                                    .orElse(null);

                            if (matchingSegment != null) {
                                fareSegment.setFareBasis(matchingSegment.getFareBasis());
                                fareSegment.setBookingClass(matchingSegment.getBookingClass());
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("Error while updatePaxFareDetailList ", e);
                throw new RuntimeException(e);
            }
    }


    private List<PAXFareDetails> updateOriginalPaxFareDetailsWithSelectedRbd(List<SelectedRbdOption> selectedRbdOption, List<PAXFareDetails> originalPaxFareDetailsList) {

        try {
            if (selectedRbdOption == null || selectedRbdOption.isEmpty() ||
                    originalPaxFareDetailsList == null || originalPaxFareDetailsList.isEmpty()) {
                return originalPaxFareDetailsList;
            }

            //  Build the PassengerTypemap
            Map<PassengerTypeCode, Map<String, FareSegment>> selectedSegmentMap = new HashMap<>();

            for (SelectedRbdOption option : selectedRbdOption) {
                for (PAXFareDetails selectedPax : option.getPaxFareDetailsList()) {
                    PassengerTypeCode paxType = selectedPax.getPassengerTypeCode();
                    selectedSegmentMap.putIfAbsent(paxType, new HashMap<>());

                    for (FareJourney selectedJourney : selectedPax.getFareJourneyList()) {
                        for (FareSegment selectedSegment : selectedJourney.getFareSegmentList()) {
                            selectedSegmentMap.get(paxType).put(selectedSegment.getSegment(), selectedSegment);
                        }
                    }
                }
            }

            //  Update original list using the map
            for (PAXFareDetails pax : originalPaxFareDetailsList) {
                PassengerTypeCode paxType = pax.getPassengerTypeCode();

                Map<String, FareSegment> segmentMap = selectedSegmentMap.get(paxType);
                if (segmentMap == null) continue;

                for (FareJourney journey : pax.getFareJourneyList()) {
                    for (FareSegment segment : journey.getFareSegmentList()) {
                        FareSegment selectedSegment = segmentMap.get(segment.getSegment());
                        if (selectedSegment != null) {
                            segment.setFareBasis(selectedSegment.getFareBasis());
                            segment.setCabinClass(selectedSegment.getCabinClass());
                            segment.setAvailableSeats(selectedSegment.getAvailableSeats());
                            segment.setBookingClass(selectedSegment.getBookingClass());
                        }
                    }
                }
            }

            logger.info("Original PaxFareDetails successfully updated with selected RBD options.");
            return originalPaxFareDetailsList;

        } catch (Exception e) {
            logger.error("Failed to update original PaxFareDetails with selected RBD options.", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Extracts passenger pricing details (base fare, total fare, baggage allowance, fare basis)
     * from the Amadeus FareInformativePricingWithoutPNRReply response.
     */

    public List<PassengerPricingDetail> getPricingDetailsforSelectedRbd( List<FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup> pricingGroupLevelGroup){
            List<PassengerPricingDetail> pricingDetails = new ArrayList<>();

        try {
        if (pricingGroupLevelGroup != null) {
            for (FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup pricingGrp : pricingGroupLevelGroup) {

                PassengerPricingDetail detail = new PassengerPricingDetail();
                List<UpsellSegmentDetail> segmentDetails = new ArrayList<>();


                //  Fetch Passenger & Segment Info
                List<FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup.FareInfoGroup.SegmentLevelGroup> segmentLevelGroups =
                        pricingGrp.getFareInfoGroup().getSegmentLevelGroup();

                if (segmentLevelGroups != null) {
                    for (FareInformativePricingWithoutPNRReply.MainGroup.PricingGroupLevelGroup.FareInfoGroup.SegmentLevelGroup segmentGroup
                            : segmentLevelGroups) {

                        UpsellSegmentDetail segDetail = new UpsellSegmentDetail();


                        // Flight & Route Info
                        if (segmentGroup.getSegmentInformation() != null) {
                            segDetail.setOrigin(segmentGroup.getSegmentInformation().getBoardPointDetails().getTrueLocationId());
                            segDetail.setDestination(segmentGroup.getSegmentInformation().getOffpointDetails().getTrueLocationId());
                            segDetail.setFlightNumber(segmentGroup.getSegmentInformation().getFlightIdentification().getFlightNumber());
                            segDetail.setBookingClass(segmentGroup.getSegmentInformation().getFlightIdentification().getBookingClass());
                            segDetail.setDepatureTime(segmentGroup.getSegmentInformation().getFlightDate().getDepartureDate());
                        }

                        // Fetch Passenger Type
                        if (segmentGroup.getPtcSegment() != null &&
                                segmentGroup.getPtcSegment().getQuantityDetails() != null) {
                            detail.setPassengerType(segmentGroup.getPtcSegment().getQuantityDetails().getUnitQualifier());
                        }

                        // Fetch Baggage Allowance
                        if (segmentGroup.getBaggageAllowance() != null &&
                                segmentGroup.getBaggageAllowance().getBaggageDetails() != null) {
                            BaggageDetailsTypeI baggageDetails = segmentGroup.getBaggageAllowance().getBaggageDetails();
                            BigInteger freeAllowance = baggageDetails.getFreeAllowance();
                            String unit = AmadeusFlightInfoServiceImpl.baggageCodes
                                    .getOrDefault(baggageDetails.getQuantityCode(), baggageDetails.getQuantityCode());
                            segDetail.setBaggageAllowance(freeAllowance.toString() +" "+ unit);
                        }

                        // Fetch Fare Basis
                        if (segmentGroup.getFareBasis() != null && segmentGroup.getFareBasis().getAdditionalFareDetails() != null) {
                            segDetail.setFareBasis(segmentGroup.getFareBasis().getAdditionalFareDetails().getRateClass());
                        }

                        segmentDetails.add(segDetail);

                    }
                }
                detail.setSegments(segmentDetails);

                // Fetch Fare Information
                if (pricingGrp.getFareInfoGroup() != null &&
                        pricingGrp.getFareInfoGroup().getFareAmount() != null) {

                    MonetaryInformationType157222S fareAmount = pricingGrp.getFareInfoGroup().getFareAmount();

                    boolean isBaseFareInINR = false;

//           Fetch Base Fare
                    if (fareAmount.getMonetaryDetails() != null) {
                        MonetaryInformationDetailsType223866C monetaryDetails = fareAmount.getMonetaryDetails();
                        if ("B".equalsIgnoreCase(monetaryDetails.getTypeQualifier()) || "E".equalsIgnoreCase(monetaryDetails.getTypeQualifier())) {
                            if ("INR".equalsIgnoreCase(monetaryDetails.getCurrency())) {
                                detail.setBaseFare(new BigDecimal(monetaryDetails.getAmount()));
                                detail.setCurrency(monetaryDetails.getCurrency());
                                isBaseFareInINR = true;
                            } else {
                                logger.warn("Base fare currency is not INR, it is: {}", monetaryDetails.getCurrency());
                            }
                        }
                    }

//          If base fare not fetched from primary monetaryDetails, check otherMonetaryDetails
                    if (!isBaseFareInINR && fareAmount.getOtherMonetaryDetails() != null) {
                        for (MonetaryInformationDetailsType223866C otherDetail : fareAmount.getOtherMonetaryDetails()) {
                            if (("B".equalsIgnoreCase(otherDetail.getTypeQualifier()) || "E".equalsIgnoreCase(otherDetail.getTypeQualifier()))
                                    && "INR".equalsIgnoreCase(otherDetail.getCurrency())) {
                                detail.setBaseFare(new BigDecimal(otherDetail.getAmount()));
                                detail.setCurrency(otherDetail.getCurrency());
                                isBaseFareInINR = true;
                                logger.warn("Base fare fetched from otherMonetaryDetails with INR currency: {}", otherDetail.getAmount());
                                break;
                            }
                        }
                    }


//                    if still not fetched, log a final warning
                    if (!isBaseFareInINR) {
                        logger.warn("Base fare in INR not found in any monetary details.");
                    }



                    // Fetch Total Fare
                    List<MonetaryInformationDetailsType223866C> otherMonetaryDetails = fareAmount.getOtherMonetaryDetails();
                    if (otherMonetaryDetails != null) {
                        for (MonetaryInformationDetailsType223866C otherDetail : otherMonetaryDetails) {
                            if (AmadeusConstants.TOTAL_FARE_IDENTIFIER.equals(otherDetail.getTypeQualifier())) {
                                detail.setTotalFare(new BigDecimal(otherDetail.getAmount()));
                                detail.setCurrency(otherDetail.getCurrency());
                            }
                        }
                    }
                }

                pricingDetails.add(detail);
            }
        }

        logger.info("Extracted GDS Pricing Details for selectedRBDs: {}", pricingDetails);

        return pricingDetails;

    } catch (Exception e) {
        logger.error("Error while extracting pricing details from FareInformativePricingWithoutPNRReply", e);
        throw new RuntimeException("Failed to extract pricing details for selectedRBDs", e);
    }
    }

}