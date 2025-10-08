package services.indigo;

import com.compassites.model.*;
import com.compassites.model.traveller.TravellerMasterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.IndigoPaxNumber;
import dto.refund.IndigoRefundRequest;
import dto.reissue.ReIssueSearchRequest;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndigoFlightServiceImpl implements IndigoFlightService {

    static Logger logger = LoggerFactory.getLogger("gds");

    static Logger indigoLogger = LoggerFactory.getLogger("indigo");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final String endPoint = Play.application().configuration().getString("indigo.service.endPoint");

    @Override
    public PNRResponse checkFareChangeAndAvailability(TravellerMasterInfo travellerMasterInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "checkFare").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.debug("Indigo Fare Change Response: " + responseBody);
                    return objectMapper.readValue(responseBody, PNRResponse.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for traveller info: " + Json.toJson(travellerMasterInfo));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo fare change check: " + e.getMessage() +
                    " for traveller info: " + Json.toJson(travellerMasterInfo), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public PNRResponse generatePNR(TravellerMasterInfo travellerMasterInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "generatePNR").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.debug("Indigo PNR Generation Response: " + responseBody);
                    return objectMapper.readValue(responseBody, PNRResponse.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for traveller info: " + Json.toJson(travellerMasterInfo));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo fare change check: " + e.getMessage() +
                    " for traveller info: " + Json.toJson(travellerMasterInfo), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public IssuanceResponse priceBookedPNR(IssuanceRequest issuanceRequest) {
        try {
            logger.info("Indigo price booked PNR request: " + Json.toJson(issuanceRequest));
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(issuanceRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "priceBookedPNR").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.debug("Indigo Price Booked PNR Response: " + responseBody);
                    return objectMapper.readValue(responseBody, IssuanceResponse.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for traveller info: " + Json.toJson(issuanceRequest));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo price booked PNR: " + e.getMessage() +
                    " for issuance request: " + Json.toJson(issuanceRequest), e);
        }
        return null;
    }

    @Override
    public IssuanceResponse issueTicket(IssuanceRequest issuanceRequest) {
        logger.debug("Indigo issue ticket request: " + Json.toJson(issuanceRequest));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(issuanceRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "issueTicket").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.debug("Indigo Issue Ticket Response: " + responseBody);
                    return objectMapper.readValue(responseBody, IssuanceResponse.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for issuance request: " + Json.toJson(issuanceRequest));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo issue ticket: " + e.getMessage() +
                    " for issuance request: " + Json.toJson(issuanceRequest), e);
        }
        return null;
    }

    @Override
    public AncillaryServicesResponse getAvailableAncillaryServices(TravellerMasterInfo travellerMasterInfo) {
        logger.info("Indigo get available ancillary services request: " + Json.toJson(travellerMasterInfo));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "showAdditionalBaggageInfoStandalone").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.debug("Indigo Issue Ticket Response: " + responseBody);
                    return objectMapper.readValue(responseBody, AncillaryServicesResponse.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for issuance request: " + Json.toJson(travellerMasterInfo));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo get available ancillary services: " + e.getMessage() +
                    " for traveller info: " + Json.toJson(travellerMasterInfo), e);
        }
        return null;
    }

    @Override
    public SearchResponse getReissueSearchResponse(ReIssueSearchRequest reIssueSearchRequest) {

        logger.info("Indigo Reissue Search request: {}", Json.toJson(reIssueSearchRequest));

        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setReIssueSearch(true);

        List<ErrorMessage> errorMessageList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            String reIssueSearchRequestJsonString = objectMapper.writeValueAsString(reIssueSearchRequest);
            RequestBody requestBody = RequestBody.create(reIssueSearchRequestJsonString, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder().url(endPoint + "reIssueSearch").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    searchResponse = objectMapper.readValue(responseBody, SearchResponse.class);

                } else {
                    ErrorMessage errorMessage = new ErrorMessage();
                    errorMessage.setErrorCode("Error While Searching For ReIssuable flights");
                    errorMessageList.add(errorMessage);

                    searchResponse.setErrorMessageList(errorMessageList);
                }

                return searchResponse;
            }

        } catch (Exception exception) {
            logger.debug("Exception while Searching for ReIssuable flights");

            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setErrorCode("Error While Searching For ReIssuable flights");
            errorMessageList.add(errorMessage);

            searchResponse.setErrorMessageList(errorMessageList);
            return searchResponse;
        }
    }

    @Override
    public TicketCheckEligibilityRes processFullCancellation(String gdsPNR, String searchOfficeId, String ticketingOfficeId, List<String> ticketIdsList) {
        logger.info("Indigo process reissue ticket request for PNR: " + gdsPNR);
        try {
            IndigoRefundRequest indigoRefundRequest = new IndigoRefundRequest();
            indigoRefundRequest.setGdsPNR(gdsPNR);
            indigoRefundRequest.setSearchOfficeId(searchOfficeId);
            indigoRefundRequest.setTicketingOfficeId(ticketingOfficeId);
            if(ticketIdsList != null && ticketIdsList.size() > 0){
                indigoRefundRequest.setTicketIdsList(ticketIdsList);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(indigoRefundRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "fullRefundEligibility").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.info("Indigo Full Cancellation Response: " + responseBody);
                    return objectMapper.readValue(responseBody, TicketCheckEligibilityRes.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for issuance request: " + Json.toJson(indigoRefundRequest));
                    throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo full cancellation: " + e.getMessage() +
                    " for PNR: " + gdsPNR, e);
        }
        return null;
    }

    public TicketProcessRefundRes processFullRefund(String gdsPNR, String searchOfficeId, String ticketingOfficeId,TravellerMasterInfo travellerMasterInfo) {
        logger.info("Indigo process reissue ticket request for PNR processFullRefund : " + gdsPNR);
        try {
            IndigoRefundRequest indigoRefundRequest = new IndigoRefundRequest();
            indigoRefundRequest.setGdsPNR(gdsPNR);
            indigoRefundRequest.setSearchOfficeId(searchOfficeId);
            indigoRefundRequest.setTicketingOfficeId(ticketingOfficeId);
            indigoRefundRequest.setTravellerMasterInfo(travellerMasterInfo);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(indigoRefundRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "fullRefund").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.info("Indigo Full Cancellation Response processFullRefund : " + responseBody);
                    return objectMapper.readValue(responseBody, TicketProcessRefundRes.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API processFullRefund: " + response.message() +
                            " for issuance request: " + Json.toJson(indigoRefundRequest));
                    throw new Exception("Failed to fetch data from Indigo API processFullRefund: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo full cancellation processFullRefund : " + e.getMessage() +
                    " for PNR: " + gdsPNR, e);
        }
        return null;
    }

    @Override
    public TicketCheckEligibilityRes checkPartRefundTicketEligibilityForIndigo(List<String> ticketList, String gdspnr, String searchOfficeId, String ticketingOfficeId, List<String> ticketIdsList) {
        return null;
    }

    @Override
    public TicketProcessRefundRes processPartialRefund(String gdsPNR, String searchOfficeId, String ticketingOfficeId, List<String> ticketList, List<IndigoPaxNumber> indigoPaxNumbers, TravellerMasterInfo travellerMasterInfo) {
        logger.info("Indigo process partial refund request for PNR: " + gdsPNR);
        try {
            IndigoRefundRequest indigoRefundRequest = new IndigoRefundRequest();
            indigoRefundRequest.setGdsPNR(gdsPNR);
            indigoRefundRequest.setSearchOfficeId(searchOfficeId);
            indigoRefundRequest.setTicketingOfficeId(ticketingOfficeId);
            indigoRefundRequest.setTravellerMasterInfo(travellerMasterInfo);
            indigoRefundRequest.setIndigoPaxNumbers(indigoPaxNumbers);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(indigoRefundRequest);
            logger.debug("Indigo Partial Refund Request: " + jsonString);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "processPartialRefund").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    indigoLogger.info("Indigo Partial Cancellation Response processFullRefund : " + responseBody);
                    return objectMapper.readValue(responseBody, TicketProcessRefundRes.class);
                } else {
                    logger.error("Failed to fetch data from Indigo API processFullRefund: " + response.message() +
                            " for issuance request: " + Json.toJson(indigoRefundRequest));
                    throw new Exception("Failed to fetch data from Indigo API processFullRefund: " + response.message());
                }
            }

        } catch (Exception e) {
            logger.error("Error during Indigo partial refund: " + e.getMessage() +
                    " for PNR: " + gdsPNR, e);
        }
        return null;
    }
}
