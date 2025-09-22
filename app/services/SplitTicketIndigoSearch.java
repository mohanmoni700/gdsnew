package services;

import com.compassites.GDSWrapper.amadeus.ServiceHandler;
import com.compassites.model.FlightItinerary;
import com.compassites.model.SearchParameters;
import com.compassites.model.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import ennum.ConfigMasterConstants;
import models.FlightSearchOffice;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import play.Play;
import play.libs.Json;
import utils.AmadeusSessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SplitTicketIndigoSearch implements SplitTicketSearch{

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ConfigurationMasterService configurationMasterService;
    static org.slf4j.Logger amadeusLogger = LoggerFactory.getLogger("amadeus");

    @Autowired
    private ServiceHandler serviceHandler;

    @Autowired
    private AmadeusSessionManager amadeusSessionManager;

    @Autowired
    private AmadeusSourceOfficeService sourceOfficeService;

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //private String searchOfficeID = play.Play.application().configuration().getString("split.ticket.officeId");
    private static String searchOfficeID = "";
    static org.slf4j.Logger logger = LoggerFactory.getLogger("gds");
    private static final OkHttpClient client = new OkHttpClient();
    private static final String endPoint = Play.application().configuration().getString("indigo.service.endPoint");
    static Logger indigoLogger = LoggerFactory.getLogger("indigo");
    @Override
    public List<SearchResponse> splitSearch(List<SearchParameters> searchParameters, ConcurrentHashMap<String, List<FlightItinerary>> concurrentHashMap, boolean isDomestic) throws Exception {
        List<SearchResponse> responses = new ArrayList<>();
        searchOfficeID = configurationMasterService.getConfig(ConfigMasterConstants.SPLIT_TICKET_AMADEUS_OFFICE_ID_GLOBAL.getKey());
        
        logger.info("Indigo split ticket - Starting search for {} parameters", searchParameters.size());
        System.out.println("Indigo split ticket - Starting search for " + searchParameters.size() + " parameters");
        
        for (int i = 0; i < searchParameters.size(); i++) {
            SearchParameters searchParameters1 = searchParameters.get(i);
            String from = searchParameters1.getJourneyList().get(0).getOrigin();
            String to = searchParameters1.getJourneyList().get(searchParameters1.getJourneyList().size()-1).getDestination();
            String route = from + " to " + to;
            
            long searchStartTime = System.currentTimeMillis();
            logger.info("Indigo split ticket - Search {} started at: {} - Route: {}", i + 1, new Date(searchStartTime), route);
            System.out.println("Indigo split ticket - Search " + (i + 1) + " started at: " + new Date(searchStartTime) + " - Route: " + route);
            FlightSearchOffice searchOffice = new FlightSearchOffice();
            searchOffice.setOfficeId("Indigo");
            searchOffice.setName("");
            if(searchOffice==null){
                logger.error("Invalid Indigo Office Id " + searchOfficeID + " provided for Split Ticketing");
                SearchResponse searchResponse = new SearchResponse();
                searchResponse.setFlightSearchOffice(searchOffice);
                searchResponse.setProvider("Indigo");
                responses.add(searchResponse);
            } else {
                SearchResponse searchResponse = search(searchParameters1, searchOffice);
                //System.out.println("Indigo split ticket - Search " + (i + 1) + " completed for route " + searchResponse);
                
                // Check if there are flights in any of the hashmaps or flightItineraryList
                boolean hasFlights = (searchResponse.getAirSolution().getFlightItineraryList() != null && searchResponse.getAirSolution().getFlightItineraryList().size() > 0) ||
                                   (searchResponse.getAirSolution().getSeamenHashMap() != null && searchResponse.getAirSolution().getSeamenHashMap().size() > 0) ||
                                   (searchResponse.getAirSolution().getNonSeamenHashMap() != null && searchResponse.getAirSolution().getNonSeamenHashMap().size() > 0);
                
                logger.info("Indigo search result - hasFlights: {}, flightItineraryList: {}, seamenHashMap: {}, nonSeamenHashMap: {}", 
                           hasFlights,
                           searchResponse.getAirSolution().getFlightItineraryList() != null ? searchResponse.getAirSolution().getFlightItineraryList().size() : 0,
                           searchResponse.getAirSolution().getSeamenHashMap() != null ? searchResponse.getAirSolution().getSeamenHashMap().size() : 0,
                           searchResponse.getAirSolution().getNonSeamenHashMap() != null ? searchResponse.getAirSolution().getNonSeamenHashMap().size() : 0);
                System.out.println("Indigo search result - hasFlights: " + hasFlights + 
                                  ", flightItineraryList: " + (searchResponse.getAirSolution().getFlightItineraryList() != null ? searchResponse.getAirSolution().getFlightItineraryList().size() : 0) +
                                  ", seamenHashMap: " + (searchResponse.getAirSolution().getSeamenHashMap() != null ? searchResponse.getAirSolution().getSeamenHashMap().size() : 0) +
                                  ", nonSeamenHashMap: " + (searchResponse.getAirSolution().getNonSeamenHashMap() != null ? searchResponse.getAirSolution().getNonSeamenHashMap().values().size() : 0));
                
                if (hasFlights) {
                    if (concurrentHashMap.containsKey(searchParameters1.getJourneyList().get(0).getOrigin())) {
                        concurrentHashMap.get(searchParameters1.getJourneyList().get(0).getOrigin()).addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getSeamenHashMap().values()));
                        concurrentHashMap.get(searchParameters1.getJourneyList().get(0).getOrigin()).addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getNonSeamenHashMap().values()));
                        System.out.println("Indigo Size of non indigo seamen if "+searchResponse.getAirSolution().getNonSeamenHashMap().values().size());
                    } else {
                        List<FlightItinerary> indigoFlights = new ArrayList<>();
                        // Add both seamen and non-seamen flights
                        indigoFlights.addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getSeamenHashMap().values()));
                        indigoFlights.addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getNonSeamenHashMap().values()));
                        concurrentHashMap.put(searchParameters1.getJourneyList().get(0).getOrigin(), indigoFlights);
                        System.out.println("Indigo Size of indigo seamen: " + searchResponse.getAirSolution().getSeamenHashMap().values().size());
                        System.out.println("Indigo Size of indigo non seamen: " + searchResponse.getAirSolution().getNonSeamenHashMap().values().size());
                        System.out.println("Indigo Total indigo flights added: " + indigoFlights.size());
                    }
                }
                
                // Always add the search response to responses, even if no flights found
                // This ensures Indigo results are included in allSearchResponses
                responses.add(searchResponse);
                logger.info("Added Indigo SearchResponse to responses list. Total responses: {}", responses.size());
                System.out.println("Added Indigo SearchResponse to responses list. Total responses: " + responses.size());
                
                long searchEndTime = System.currentTimeMillis();
                long searchDuration = searchEndTime - searchStartTime;
                logger.info("Indigo split ticket - Search {} completed at: {} (Duration: {} seconds) - Route: {}", 
                           i + 1, new Date(searchEndTime), searchDuration/1000, route);
                System.out.println("Indigo split ticket - Search " + (i + 1) + " completed at: " + new Date(searchEndTime) + 
                                 " (Duration: " + searchDuration/1000 + " seconds) - Route: " + route);
            }
        }
        System.out.println("indigo split search response size: "+responses.size());
        System.out.println("indigo split search concurrentHashMap size: "+concurrentHashMap.size());
        for (Map.Entry<String, List<FlightItinerary>> flightItineraryEntry : concurrentHashMap.entrySet()) {
            logger.debug("flightItineraryEntry size: "+flightItineraryEntry.getKey()+"  -  "+flightItineraryEntry.getValue().size());
            System.out.println("indigo size: "+flightItineraryEntry.getKey()+"  -  "+flightItineraryEntry.getValue().size());
            if(flightItineraryEntry.getValue().size() == 0) {
                concurrentHashMap.remove(flightItineraryEntry.getKey());
            }
        }
        logger.info("indigo split search response size: "+responses.size());
        logger.info("indigo split search concurrentHashMap size: "+concurrentHashMap.size());

        System.out.println("indigo split search response size: "+responses.size());
        System.out.println("indigo split search concurrentHashMap size: "+concurrentHashMap.size());
        logger.info("indigo split search concurrentHashMap details response : "+ Json.toJson(responses));
        logger.info("indigo split search concurrentHashMap details: "+ Json.toJson(concurrentHashMap));
        return responses;
    }

    public SearchResponse search(SearchParameters searchParameters, FlightSearchOffice office) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(searchParameters);
            RequestBody requestBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(endPoint+"flightSearch").post(requestBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    SearchResponse searchResponse = objectMapper.readValue(responseBody, SearchResponse.class);
                    indigoLogger.debug("Indigo Flight Search Response: " + responseBody);
                    System.out.println("Indigo search response ");
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Indigo");
                    return searchResponse;
                } else {
                    System.out.println("Indigo search response failed");
                    logger.error("Failed to fetch data from Indigo API: " + response.message() +
                            " for search parameters: " + Json.toJson(searchParameters));
                    SearchResponse searchResponse = new SearchResponse();
                    searchResponse.setFlightSearchOffice(office);
                    searchResponse.setProvider("Indigo");
                    return searchResponse;
                    //throw new Exception("Failed to fetch data from Indigo API: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("Error during Indigo flight search: " + e.getMessage() +
                    " for search parameters: " + Json.toJson(searchParameters), e);
            System.out.println("Indigo search response exception "+e.getMessage());
            //e.printStackTrace();
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setFlightSearchOffice(office);
            searchResponse.setProvider("Indigo");
            return searchResponse;
        }
    }
}
