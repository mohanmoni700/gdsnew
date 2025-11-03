package services.russian;

import com.compassites.constants.IndigoConstants;
import com.compassites.exceptions.RetryException;
import com.compassites.model.PNRResponse;
import com.compassites.model.SearchJourney;
import com.compassites.model.SearchParameters;
import com.compassites.model.SearchResponse;
import com.compassites.model.traveller.TravellerMasterInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Airport;
import models.FlightSearchOffice;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;
import services.FlightSearch;
import services.RetryOnFailure;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Service
public class RussianFlightSearch implements FlightSearch {

    private static final String endPoint = Play.application().configuration().getString("russian.service.endPoint");

    static Logger logger = LoggerFactory.getLogger("gds");
    static Logger russianlog = LoggerFactory.getLogger("russian");

    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @RetryOnFailure(attempts = 2, delay = 2000, exception = RetryException.class)
    @Override
    public SearchResponse search(SearchParameters searchParameters, FlightSearchOffice office) throws Exception {
        try {

            if(!isRussianSector(searchParameters)){
                return new SearchResponse();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(searchParameters);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint+"/flightSearch").post(requestBody).build();
            logger.debug("Russian Search Initiation {}  {}",new Date(), jsonString);
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    SearchResponse searchResponse = objectMapper.readValue(responseBody, SearchResponse.class);
                    logger.debug("Russian Flight Search Response: " + responseBody);
                    russianlog.info("Russian Flight Search Response: " + responseBody);
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Russian");
                    return searchResponse;
                } else {
                    logger.error("Failed to fetch data from Russian API: " + response.message() +
                            " for search parameters: " + Json.toJson(searchParameters));
                    SearchResponse searchResponse = new SearchResponse();
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Russian");
                    return searchResponse;
                }
            }
        } catch (Exception e) {
            logger.error("Error during Russian flight search: " + e.getMessage() +
                    " for search parameters: " + Json.toJson(searchParameters), e);
            //e.printStackTrace();
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setFlightSearchOffice(office);
            searchResponse.setProvider("Russian");
            return searchResponse;
        }
    }

    @Override
    public String provider() {
        return "Russian";
    }

    @Override
    public List<FlightSearchOffice> getOfficeList() {
        FlightSearchOffice fs = new FlightSearchOffice();
        fs.setOfficeId("Russian");
        List<FlightSearchOffice> lfs = new ArrayList<>();
        lfs.add(fs);
        return lfs;
    }

    private boolean isRussianSector(SearchParameters searchParameters) {
        boolean isRussianSector = false;
        for (SearchJourney searchJourney : searchParameters.getJourneyList()) {
            Airport originAirport = Airport.findByIataCode(searchJourney.getOrigin());
            Airport destinationAirport = Airport.findByIataCode(searchJourney.getDestination());
            if (originAirport.getCountry() != null && originAirport.getCountry() != "" && originAirport.getIso_country().equalsIgnoreCase("RU")) {
                isRussianSector = true;
                break;
            }
            if (destinationAirport.getCountry() != null && destinationAirport.getCountry() != "" && destinationAirport.getIso_country().equalsIgnoreCase("RU")) {
                isRussianSector = true;
                break;
            }
        }
        return isRussianSector;
    }
}
