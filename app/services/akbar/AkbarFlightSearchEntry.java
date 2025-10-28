package services.akbar;

import com.compassites.constants.AkbarConstants;
import com.compassites.exceptions.RetryException;
import com.compassites.model.SearchParameters;
import com.compassites.model.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.FlightSearchOffice;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;
import services.FlightSearch;
import services.RetryOnFailure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AkbarFlightSearchEntry implements FlightSearch {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();


    private static final String endPoint = Play.application().configuration().getString("akbar.service.endPoint");

    static Logger logger = LoggerFactory.getLogger("gds");
    static Logger akbarLogger = LoggerFactory.getLogger("akbar");

    @RetryOnFailure(attempts = 2, delay = 2000, exception = RetryException.class)
    public SearchResponse search(SearchParameters searchParameters, FlightSearchOffice office) throws Exception {

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(searchParameters);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            String url = endPoint + AkbarConstants.searchEndpoint;
            Request request = new Request.Builder().url(url).post(requestBody).build();

            logger.debug("Akbar Search Initiation {}  {}", new Date(), jsonString);
            akbarLogger.debug("Akbar Search Initiation {}  {}", new Date(), jsonString);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    SearchResponse searchResponse = objectMapper.readValue(responseBody, SearchResponse.class);
                    akbarLogger.debug("Akbar Flight Search Response: {}", responseBody);
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Akbar");
                    return searchResponse;
                } else {
                    logger.error("Failed to fetch data from Akbar API: {} for search parameters: {}", response.message(), Json.toJson(searchParameters));
                    SearchResponse searchResponse = new SearchResponse();
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Akbar");

                    return searchResponse;
                }
            }
        } catch (Exception e) {
            logger.error("Error during Akbar flight search: {} for search parameters: {}", e.getMessage(), Json.toJson(searchParameters), e);
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setFlightSearchOffice(office);
            searchResponse.setProvider("Akbar");
            return searchResponse;
        }
    }

    @Override
    public String provider() {
        return "Akbar";
    }

    @Override
    public List<FlightSearchOffice> getOfficeList() {

        FlightSearchOffice flightSearchOffice = new FlightSearchOffice();

        flightSearchOffice.setOfficeId(AkbarConstants.officeId);
        List<FlightSearchOffice> flightSearchOfficeList = new ArrayList<>();
        flightSearchOfficeList.add(flightSearchOffice);

        return flightSearchOfficeList;
    }
}
