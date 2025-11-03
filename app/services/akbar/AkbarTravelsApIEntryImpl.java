package services.akbar;

import com.compassites.constants.AkbarConstants;
import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.FareCheckRulesResponse;
import dto.refund.AkbarCancelOrRefundRequest;
import dto.refund.GetRefundAmountRequest;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AkbarTravelsApIEntryImpl implements AkbarTravelsApIEntry {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();


    private static final String endPoint = Play.application().configuration().getString("akbar.service.endPoint");

    static Logger logger = LoggerFactory.getLogger("gds");
    static Logger akbarLogger = LoggerFactory.getLogger("akbar");


    @Override
    public FlightItinerary getFreeBaggageInfo(TravellerMasterInfo travellerMasterInfo) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.getFreeBaggageInfoEndpoint;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Free Baggage Info Request: {}", jsonString);
            logger.info("Akbar Free Baggage Info Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    FlightItinerary flightItinerary = objectMapper.readValue(responseBody, FlightItinerary.class);
                    akbarLogger.debug("Akbar Free Baggage Info Response: {}", responseBody);

                    return flightItinerary;
                } else {
                    logger.error("Failed to fetch Free baggage info from Akbar API: {} for flight itinerary: {}", response.message(), Json.toJson(travellerMasterInfo.getItinerary()));
                    throw new Exception("Failed to fetch Free baggage info from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Akbar Free baggage info retrieval: {}", e.getMessage(), e);
            return null;
        }
    }


    @Override
    public Map<String, FareCheckRulesResponse> getFareRuleInfo(TravellerMasterInfo travellerMasterInfo) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.fareRulesEndpoint;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Fare Rule Info Request: {}", jsonString);
            logger.info("Akbar Fare Rule Info Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Map<String, FareCheckRulesResponse> fareRulesMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, FareCheckRulesResponse>>() {
                    });
                    akbarLogger.debug("Akbar Fare Rule Info Response: {}", responseBody);

                    return fareRulesMap;
                } else {
                    logger.error("Failed to fetch Fare Rule info from Akbar API: {} for flight itinerary: {}", response.message(), Json.toJson(travellerMasterInfo.getItinerary()));
                    throw new Exception("Failed to fetch Fare Rule info from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Fare Rule info retrieval: {}", e.getMessage(), e);
            return null;
        }
    }


    @Override
    public PNRResponse checkFareChangeAndFlightAvailability(TravellerMasterInfo travellerMasterInfo) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.checkFareChangeAndFlightAvailability;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Fare Change and Flight Availability Info Request: {}", jsonString);
            logger.info("Akbar Fare Change and Flight Availability Info Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    PNRResponse pnrResponse = objectMapper.readValue(responseBody, PNRResponse.class);
                    akbarLogger.debug("Akbar Fare Change and Flight Availability Info Response: {}", responseBody);

                    return pnrResponse;
                } else {
                    logger.error("Failed to fetch Fare Change and Flight Availability info from Akbar API: {} for flight itinerary: {}", response.message(), Json.toJson(travellerMasterInfo.getItinerary()));
                    throw new Exception("Failed to fetch Fare Change and Flight Availability info from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Fare Change and Flight Availability info retrieval: {}", e.getMessage(), e);
            return null;
        }
    }


    @Override
    public PNRResponse generatePnr(TravellerMasterInfo travellerMasterInfo) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.generatePnr;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Generate PNR Request: {}", jsonString);
            logger.info("Akbar Generate PNR Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    PNRResponse pnrResponse = objectMapper.readValue(responseBody, PNRResponse.class);
                    akbarLogger.debug("Akbar Generate PNR Response: {}", responseBody);

                    return pnrResponse;
                } else {
                    logger.error("Failed to Generate PNR from Akbar API: {} for flight itinerary: {}", response.message(), Json.toJson(travellerMasterInfo.getItinerary()));
                    throw new Exception("Failed to Generate PNR from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Generate PNR: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public IssuanceResponse completePaymentAndIssueTicket(IssuanceRequest issuanceRequest) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(issuanceRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.completePaymentAndIssueTicket;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Complete Payment Request: {}", jsonString);
            logger.info("Akbar Complete Payment Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    IssuanceResponse issuanceResponse = objectMapper.readValue(responseBody, IssuanceResponse.class);
                    akbarLogger.debug("Akbar Complete Payment Response: {}", responseBody);

                    return issuanceResponse;
                } else {
                    logger.error("Failed to Complete Payment from Akbar API: {} for TUI: {}", response.message(), Json.toJson(issuanceRequest.getAkbarTui()));
                    throw new Exception("Failed to Complete Payment from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Complete Payment: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AncillaryServicesResponse getPaidAncillaryAtPaxInfoPage(TravellerMasterInfo travellerMasterInfo) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.getPaidSSR;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Paid Ancillary Request: {}", jsonString);
            logger.info("Akbar Paid Ancillary Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    AncillaryServicesResponse ancillaryServicesResponse = objectMapper.readValue(responseBody, AncillaryServicesResponse.class);
                    akbarLogger.debug("Akbar Paid Ancillary Response: {}", responseBody);

                    return ancillaryServicesResponse;
                } else {
                    logger.error("Failed to Paid Ancillary from Akbar API: {} for TUI: {}", response.message(), Json.toJson(travellerMasterInfo.getItinerary()));
                    throw new Exception("Failed to Paid Ancillary from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Paid Ancillary : {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public TicketCheckEligibilityRes getRefundableAmount(GetRefundAmountRequest getRefundAmountRequest) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(getRefundAmountRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.getRefundableAmount;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Get Refund Amount Request: {}", jsonString);
            logger.info("Akbar Get Refund Amount Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    TicketCheckEligibilityRes ticketCheckEligibilityRes = objectMapper.readValue(responseBody, TicketCheckEligibilityRes.class);
                    akbarLogger.debug("Akbar Get Refund Amount Response: {}", responseBody);

                    return ticketCheckEligibilityRes;
                } else {
                    logger.error("Failed to Get Refund Amount from Akbar API: {} for TUI: {}", response.message(), Json.toJson(getRefundAmountRequest.getTui()));
                    throw new Exception("Failed to Get Refund Amount from Akbar API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Get Refund Amount: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public TicketProcessRefundRes confirmRefund(AkbarCancelOrRefundRequest akbarCancelOrRefundRequest) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(akbarCancelOrRefundRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.confirmRefund;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            akbarLogger.info("Akbar Confirm Refund Request: {}", jsonString);
            logger.info("Akbar Confirm Refund Request: {}", jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    TicketProcessRefundRes ticketProcessRefundRes = objectMapper.readValue(responseBody, TicketProcessRefundRes.class);
                    akbarLogger.debug("Akbar Confirm Refund Response: {}", responseBody);

                    return ticketProcessRefundRes;
                } else {
                    logger.error("Failed to Confirm Refund from Akbar API: {} for TUI: {}", response.message(), Json.toJson(akbarCancelOrRefundRequest.getTui()));

                    TicketProcessRefundRes ticketProcessRefundRes = new TicketProcessRefundRes();
                    ErrorMessage errorMessage = new ErrorMessage();
                    errorMessage.setErrorCode("Confirm Refund Failed");
                    ticketProcessRefundRes.setMessage(errorMessage);
                    ticketProcessRefundRes.setStatus(false);

                    return ticketProcessRefundRes;
                }
            }
        } catch (Exception e) {
            logger.error("Error during Confirm Refund: {}", e.getMessage(), e);

            TicketProcessRefundRes ticketProcessRefundRes = new TicketProcessRefundRes();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorCode("Confirm Refund Failed : " + e.getMessage());
            ticketProcessRefundRes.setMessage(errorMessage);
            ticketProcessRefundRes.setStatus(false);

            return ticketProcessRefundRes;
        }
    }

}
