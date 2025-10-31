package services.russian;

import com.compassites.model.IssuanceRequest;
import com.compassites.model.IssuanceResponse;
import com.compassites.model.PNRResponse;
import com.compassites.model.traveller.TravellerMasterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;

import java.util.concurrent.TimeUnit;

@Service
public class RussianFlightServiceImpl implements RussianFlightService{

    static Logger logger = LoggerFactory.getLogger("gds");

    static Logger russianLogger = LoggerFactory.getLogger("russian");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final String endPoint = Play.application().configuration().getString("russian.service.endPoint");

    @Override
    public PNRResponse checkFareChangeAndAvailability(TravellerMasterInfo travellerMasterInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(travellerMasterInfo);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "/checkFare").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    russianLogger.debug("Russian Fare Change Response: " + responseBody);
                    return objectMapper.readValue(responseBody, PNRResponse.class);
                } else {
                    logger.error("Failed to fetch data from Russian API: " + response.message() +
                            " for traveller info: " + Json.toJson(travellerMasterInfo));
                    throw new Exception("Failed to fetch data from Russian API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Russian fare change check: " + e.getMessage() +
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
            Request request = new Request.Builder().url(endPoint + "/generatePNR").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    russianLogger.debug("Russian PNR Generation Response: " + responseBody);
                    return objectMapper.readValue(responseBody, PNRResponse.class);
                } else {
                    logger.error("Failed to fetch data from Russian API: " + response.message() +
                            " for traveller info: " + Json.toJson(travellerMasterInfo));
                    throw new Exception("Failed to fetch data from Russian API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Russian fare change check: " + e.getMessage() +
                    " for traveller info: " + Json.toJson(travellerMasterInfo), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public IssuanceResponse confirmAndIssueTicket(IssuanceRequest issuanceRequest) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(issuanceRequest);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint + "/confirmAndIssueTicket").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    russianLogger.debug("Russian IssuanceResponse: " + responseBody);
                    return objectMapper.readValue(responseBody, IssuanceResponse.class);
                } else {
                    logger.error("Failed to fetch data from Russian API: " + response.message() +
                            " for issuanceRequest: " + Json.toJson(issuanceRequest));
                    throw new Exception("Failed to fetch data from Russian API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Russian fare change check: " + e.getMessage() +
                    " for issuanceRequest: " + Json.toJson(issuanceRequest), e);
            throw new RuntimeException(e);
        }
    }
}
