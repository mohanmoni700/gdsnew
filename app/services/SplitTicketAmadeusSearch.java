package services;

import com.amadeus.xml.fmptbr_14_2_1a.*;
import com.compassites.GDSWrapper.amadeus.ServiceHandler;
import com.compassites.exceptions.IncompleteDetailsMessage;
import com.compassites.exceptions.RetryException;
import com.compassites.model.*;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import com.thoughtworks.xstream.XStream;
import dto.CabinDetails;
import ennum.ConfigMasterConstants;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import play.libs.Json;
import utils.AmadeusSessionManager;
import utils.ErrorMessageHelper;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.compassites.model.PROVIDERS.AMADEUS;
import static java.lang.String.valueOf;

import static com.compassites.model.PROVIDERS.AMADEUS;
import static java.lang.String.valueOf;

@Service
public class SplitTicketAmadeusSearch implements SplitTicketSearch{

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
    private static final Map<String,String> cabinMap = new HashMap<>();
    static {
        cabinMap.put("C","BUSINESS");
        cabinMap.put("F","FIRST");
        cabinMap.put("Y","ECONOMIC");
        cabinMap.put("W","ECONOMIC PREMIUM");
        cabinMap.put("M","ECONOMIC STANDARD");
    }
    static org.slf4j.Logger logger = LoggerFactory.getLogger("gds");
    
    // Session synchronization to prevent OptimisticLockException
    private static final Object sessionLock = new Object();
    
    // Removed semaphore bottleneck - let threads run truly in parallel
    // The session manager will handle session conflicts internally
    
    /**
     * Thread-safe session update with retry logic to handle OptimisticLockException
     */
    private void updateSessionSafely(AmadeusSessionWrapper amadeusSessionWrapper) {
        if (amadeusSessionWrapper == null) {
            return;
        }
        
        synchronized (sessionLock) {
            int maxRetries = 3;
            int retryCount = 0;
            
            while (retryCount < maxRetries) {
                try {
                    amadeusSessionManager.updateSplitTicketSessionFast(amadeusSessionWrapper);
                    logger.debug("Split ticket - Session updated successfully on attempt {}", retryCount + 1);
                    System.out.println("Split ticket - Session updated successfully on attempt " + (retryCount + 1));
                    return;
                } catch (Exception e) {
                    retryCount++;
                    
                    // Check if it's an OptimisticLockException
                    boolean isOptimisticLockException = e.getMessage() != null && 
                        (e.getMessage().contains("OptimisticLockException") || 
                         e.getClass().getSimpleName().equals("OptimisticLockException"));
                    
                    if (isOptimisticLockException) {
                        logger.warn("Split ticket - OptimisticLockException on attempt {}, retrying... ({} of {})", 
                                   retryCount, retryCount, maxRetries);
                        System.out.println("Split ticket - OptimisticLockException on attempt " + retryCount + 
                                         ", retrying... (" + retryCount + " of " + maxRetries + ")");
                        
                        if (retryCount < maxRetries) {
                            try {
                                Thread.sleep(200 * retryCount); // Progressive delay: 200ms, 400ms, 600ms
                                logger.debug("Split ticket - Retry delay completed, refreshing session and attempting again...");
                                System.out.println("Split ticket - Retry delay completed, refreshing session and attempting again...");
                                
                                // Try to refresh the session before retrying
                                try {
                                    // Get a fresh copy of the session from database
                                    AmadeusSessionWrapper refreshedSession = AmadeusSessionWrapper.findBySessionId(amadeusSessionWrapper.getSessionId());
                                    if (refreshedSession != null) {
                                        // Copy the updated data to our session object
                                        amadeusSessionWrapper.setSequenceNumber(refreshedSession.getSequenceNumber());
                                        amadeusSessionWrapper.setLastQueryDate(refreshedSession.getLastQueryDate());
                                        amadeusSessionWrapper.setQueryInProgress(refreshedSession.isQueryInProgress());
                                        logger.debug("Split ticket - Session refreshed from database");
                                        System.out.println("Split ticket - Session refreshed from database");
                                    }
                                } catch (Exception refreshEx) {
                                    logger.warn("Split ticket - Failed to refresh session, continuing with retry: {}", refreshEx.getMessage());
                                    System.out.println("Split ticket - Failed to refresh session, continuing with retry: " + refreshEx.getMessage());
                                }
                                
                                continue; // Continue the loop to retry
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                logger.error("Split ticket - Thread interrupted during retry delay");
                                System.out.println("Split ticket - Thread interrupted during retry delay");
                                return;
                            }
                        } else {
                            logger.error("Split ticket - Failed to update session after {} attempts: {}", maxRetries, e.getMessage());
                            System.out.println("Split ticket - Failed to update session after " + maxRetries + " attempts: " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        logger.error("Split ticket - Unexpected error during session update: {}", e.getMessage());
                        System.out.println("Split ticket - Unexpected error during session update: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
    @Override
    public List<SearchResponse> splitSearch(List<SearchParameters> searchParameters, ConcurrentHashMap<String, List<FlightItinerary>> concurrentHashMap, boolean isDomestic) throws Exception {
        long splitSearchStartTime = System.currentTimeMillis();
        logger.info("=== SPLIT TICKET AMADEUS SEARCH STARTED (MULTI-THREADED) ===");
        System.out.println("=== SPLIT TICKET AMADEUS SEARCH STARTED (MULTI-THREADED) ===");
        logger.info("Processing {} search parameters", searchParameters.size());
        System.out.println("Processing " + searchParameters.size() + " search parameters");
        
        List<SearchResponse> responses = new ArrayList<>();
        searchOfficeID = configurationMasterService.getConfig(ConfigMasterConstants.SPLIT_TICKET_AMADEUS_OFFICE_ID_GLOBAL.getKey());
        
        logger.info("Split ticket - Starting multi-threaded search for {} parameters", searchParameters.size());
        System.out.println("Split ticket - Starting multi-threaded search for " + searchParameters.size() + " parameters");
        
        // Create thread pool - allow more threads for true parallel execution
        // Session manager will handle conflicts internally
        int threadPoolSize = Math.min(searchParameters.size(), 6);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<SearchResponse>> futures = new ArrayList<>();
        
        logger.info("Created thread pool with {} threads", threadPoolSize);
        System.out.println("Created thread pool with " + threadPoolSize + " threads");
        
        // Submit all search tasks to thread pool for true parallel execution
        for (int i = 0; i < searchParameters.size(); i++) {
            final int index = i;
            final SearchParameters searchParameters1 = searchParameters.get(i);
            
            Future<SearchResponse> future = executor.submit(new Callable<SearchResponse>() {
                @Override
                public SearchResponse call() throws Exception {
                    String from = searchParameters1.getJourneyList().get(0).getOrigin();
                    String to = searchParameters1.getJourneyList().get(searchParameters1.getJourneyList().size()-1).getDestination();
                    String route = from + " to " + to;
                    
                    long searchStartTime = System.currentTimeMillis();
                    logger.info("Split ticket - Search {} started at: {} - Route: {} (Thread: {})", 
                               index + 1, new Date(searchStartTime), route, Thread.currentThread().getName());
                    System.out.println("Split ticket - Search " + (index + 1) + " started at: " + new Date(searchStartTime) + 
                                     " - Route: " + route + " (Thread: " + Thread.currentThread().getName() + ")");
                    
                    FlightSearchOffice searchOffice = new FlightSearchOffice();
                    searchOffice.setOfficeId(searchOfficeID);
                    searchOffice.setName("");
                    SearchResponse searchResponse = null;
                    
                    try {
                        // Run searches in parallel without semaphore bottleneck
                        logger.debug("Split ticket - Starting parallel search {} (Thread: {})", 
                                   index + 1, Thread.currentThread().getName());
                        System.out.println("Split ticket - Starting parallel search " + (index + 1) + 
                                         " (Thread: " + Thread.currentThread().getName() + ")");
                        
                        if (isDomestic) {
                            searchResponse = findNextSegmentDeparture(searchParameters1, searchOffice);
                        } else {
                            searchResponse = search(searchParameters1, searchOffice);
                        }
                        
                        long searchEndTime = System.currentTimeMillis();
                        long searchDuration = searchEndTime - searchStartTime;
                        
                        logger.info("Split ticket - Search {} completed at: {} (Duration: {} seconds) - Route: {} (Thread: {})", 
                                   index + 1, new Date(searchEndTime), searchDuration/1000, route, Thread.currentThread().getName());
                        System.out.println("Split ticket - Search " + (index + 1) + " completed at: " + new Date(searchEndTime) + 
                                         " (Duration: " + searchDuration/1000 + " seconds) - Route: " + route + " (Thread: " + Thread.currentThread().getName() + ")");
                        
                        return searchResponse;
                        
                    } catch (Exception e) {
                        long searchEndTime = System.currentTimeMillis();
                        long searchDuration = searchEndTime - searchStartTime;
                        
                        logger.error("Split ticket - Search {} failed at: {} (Duration: {} seconds) - Route: {} - Error: {} (Thread: {})", 
                                   index + 1, new Date(searchEndTime), searchDuration/1000, route, e.getMessage(), Thread.currentThread().getName());
                        System.out.println("Split ticket - Search " + (index + 1) + " failed at: " + new Date(searchEndTime) + 
                                         " (Duration: " + searchDuration/1000 + " seconds) - Route: " + route + " - Error: " + e.getMessage() + 
                                         " (Thread: " + Thread.currentThread().getName() + ")");
                        throw e;
                    }
                }
            });
            
            futures.add(future);
        }
        
        logger.info("Submitted {} search tasks to thread pool", futures.size());
        System.out.println("Submitted " + futures.size() + " search tasks to thread pool");
        
        // Collect results from all futures
        long collectionStartTime = System.currentTimeMillis();
        for (int i = 0; i < futures.size(); i++) {
            try {
                Future<SearchResponse> future = futures.get(i);
                SearchResponse searchResponse = future.get(); // This will block until the task completes
                
                if (searchResponse != null) {
                    // Update concurrentHashMap with thread-safe operations
                    String origin = searchParameters.get(i).getJourneyList().get(0).getOrigin();
                    synchronized (concurrentHashMap) {
                        if (concurrentHashMap.containsKey(origin)) {
                            concurrentHashMap.get(origin).addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getSeamenHashMap().values()));
                            concurrentHashMap.get(origin).addAll(new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getNonSeamenHashMap().values()));
                            System.out.println("Size of non seamen if "+searchResponse.getAirSolution().getNonSeamenHashMap().values().size());
                        } else {
                            concurrentHashMap.put(origin, new ArrayList<FlightItinerary>(searchResponse.getAirSolution().getSeamenHashMap().values()));
                            System.out.println("Size of non seamen else "+searchResponse.getAirSolution().getNonSeamenHashMap().values().size());
                        }
                    }
                    
                    responses.add(searchResponse);
                    logger.info("Collected result {} of {} - Route: {}", i + 1, futures.size(), 
                               searchParameters.get(i).getJourneyList().get(0).getOrigin() + " to " + 
                               searchParameters.get(i).getJourneyList().get(searchParameters.get(i).getJourneyList().size()-1).getDestination());
                    System.out.println("Collected result " + (i + 1) + " of " + futures.size());
                }
                
            } catch (Exception e) {
                logger.error("Failed to get result from future {}: {}", i + 1, e.getMessage());
                System.out.println("Failed to get result from future " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        long collectionEndTime = System.currentTimeMillis();
        long collectionDuration = collectionEndTime - collectionStartTime;
        logger.info("Result collection took: {} ms ({} seconds)", collectionDuration, collectionDuration/1000);
        System.out.println("Result collection took: " + collectionDuration + " ms (" + collectionDuration/1000 + " seconds)");
        
        // Clean up empty entries from concurrentHashMap
        synchronized (concurrentHashMap) {
            for (Map.Entry<String, List<FlightItinerary>> flightItineraryEntry : concurrentHashMap.entrySet()) {
                logger.debug("flightItineraryEntry size: "+flightItineraryEntry.getKey()+"  -  "+flightItineraryEntry.getValue().size());
                System.out.println("flightItineraryEntry size: "+flightItineraryEntry.getKey()+"  -  "+flightItineraryEntry.getValue().size());
                if(flightItineraryEntry.getValue().size() == 0) {
                    concurrentHashMap.remove(flightItineraryEntry.getKey());
                }
            }
        }
        
        // Shutdown thread pool
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                logger.warn("Thread pool did not terminate gracefully, forcing shutdown");
                System.out.println("Thread pool did not terminate gracefully, forcing shutdown");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("Thread pool shutdown interrupted", e);
            System.out.println("Thread pool shutdown interrupted: " + e.getMessage());
        }
        
        System.out.println("responses "+responses.size());
        
        long splitSearchEndTime = System.currentTimeMillis();
        long splitSearchDuration = splitSearchEndTime - splitSearchStartTime;
        logger.info("=== SPLIT TICKET AMADEUS SEARCH COMPLETED (MULTI-THREADED) ===");
        System.out.println("=== SPLIT TICKET AMADEUS SEARCH COMPLETED (MULTI-THREADED) ===");
        logger.info("Total split ticket amadeus search time: {} ms ({} seconds)", 
                   splitSearchDuration, splitSearchDuration/1000);
        System.out.println("Total split ticket amadeus search time: " + splitSearchDuration + " ms (" + splitSearchDuration/1000 + " seconds)");
        
        return responses;
    }

    public SearchResponse findNextSegmentDeparture(SearchParameters searchParameters, FlightSearchOffice office) throws Exception {
        String from = searchParameters.getJourneyList().get(0).getOrigin();
        String to = searchParameters.getJourneyList().get(searchParameters.getJourneyList().size()-1).getDestination();
        String route = from + " to " + to;
        
        long apiStartTime = System.currentTimeMillis();
        logger.debug("##################### findNextSegmentDeparture AmadeusFlightSearch started  : ");
        logger.info("Split ticket - Amadeus API call started at: {} - Route: {}", new Date(apiStartTime), route);
        System.out.println("Split ticket - Amadeus API call started at: " + new Date(apiStartTime) + " - Route: " + route);
        logger.debug("#####################SearchParameters: \n"+ Json.toJson(searchParameters));
        
        SearchResponse searchResponse = new SearchResponse();
        AmadeusSessionWrapper amadeusSessionWrapper = null;
        searchResponse.setProvider("Amadeus");
        searchResponse.setFlightSearchOffice(office);
        searchResponse.setAirSegmentKey(from+to);
        FareMasterPricerTravelBoardSearchReply fareMasterPricerTravelBoardSearchReply = null;
        FareMasterPricerTravelBoardSearchReply seamenReply = null;

        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Start "+new Date());
            amadeusSessionWrapper = amadeusSessionManager.getSplitTicketSessionFast(office);
            System.out.println("End "+new Date());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Check if session is null or has null session ID, create new session if needed
            if (amadeusSessionWrapper == null || amadeusSessionWrapper.getmSession() == null || 
                amadeusSessionWrapper.getmSession().value == null || 
                amadeusSessionWrapper.getmSession().value.getSessionId() == null || 
                amadeusSessionWrapper.getmSession().value.getSessionId().isEmpty()) {
                
                logger.warn("Session is null or has null session ID, creating new session");
                amadeusSessionWrapper = amadeusSessionManager.createSession(office);
                
                if (amadeusSessionWrapper == null) {
                    logger.error("Failed to create new Amadeus session");
                    throw new Exception("Failed to create new Amadeus session");
                }
            }

            logger.debug("...................................Amadeus Search Session used: " + Json.toJson(amadeusSessionWrapper.getmSession().value));
            logger.debug("Execution time in getting session:: " + duration/1000 + " seconds");//to be removed
            
            long searchExecutionStartTime = System.currentTimeMillis();
            logger.info("Split ticket - Starting Amadeus API call at: {} - Route: {}", new Date(searchExecutionStartTime), route);
            System.out.println("Split ticket - Starting Amadeus API call at: " + new Date(searchExecutionStartTime) + " - Route: " + route);
            
            if (searchParameters.getBookingType() == BookingType.SEAMEN) {
                seamenReply = serviceHandler.searchSplitAirlines(searchParameters, amadeusSessionWrapper,true);
                amadeusLogger.debug("AmadeusSearchRes "+ new Date()+" ------->>"+ new XStream().toXML(seamenReply));
            } else {
                fareMasterPricerTravelBoardSearchReply = serviceHandler.searchSplitAirlines(searchParameters, amadeusSessionWrapper,true);
                amadeusLogger.debug("AmadeusSearchRes "+ new Date()+" ------->>"+ new XStream().toXML(fareMasterPricerTravelBoardSearchReply));
            }
            
            long searchExecutionEndTime = System.currentTimeMillis();
            long searchExecutionDuration = searchExecutionEndTime - searchExecutionStartTime;
            logger.info("Split ticket - Amadeus API call took: {} ms ({} seconds) - Route: {}", 
                       searchExecutionDuration, searchExecutionDuration/1000, route);
            System.out.println("Split ticket - Amadeus API call took: " + searchExecutionDuration + " ms (" + searchExecutionDuration/1000 + " seconds) - Route: " + route);
        } catch (ServerSOAPFaultException soapFaultException) {

            soapFaultException.printStackTrace();
            throw new IncompleteDetailsMessage(soapFaultException.getMessage(), soapFaultException.getCause());
        } catch (ClientTransportException clientTransportException) {
            //throw new IncompleteDetailsMessage(soapFaultException.getMessage(), soapFaultException.getCause());
            ErrorMessage errorMessage = ErrorMessageHelper.createErrorMessage("partialResults", ErrorMessage.ErrorType.ERROR, "Amadeus");
            searchResponse.getErrorMessageList().add(errorMessage);
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMessage errorMessage = ErrorMessageHelper.createErrorMessage("partialResults", ErrorMessage.ErrorType.ERROR, "Amadeus");
            searchResponse.getErrorMessageList().add(errorMessage);
            return searchResponse;
        }finally {
            updateSessionSafely(amadeusSessionWrapper);
        }

        FareMasterPricerTravelBoardSearchReply.ErrorMessage seamenErrorMessage = null;
        FareMasterPricerTravelBoardSearchReply.ErrorMessage errorMessage = null;
        if (seamenReply != null) {
            seamenErrorMessage = seamenReply.getErrorMessage();
            if(seamenErrorMessage != null)
                logger.debug("seamenErrorMessage :" + seamenErrorMessage.getErrorMessageText().getDescription() + "  officeId:"+ office.getOfficeId());
        }

        if(fareMasterPricerTravelBoardSearchReply !=null) {
            errorMessage = fareMasterPricerTravelBoardSearchReply.getErrorMessage();
        }


        AirSolution airSolution = new AirSolution();
        logger.debug("#####################errorMessage is null");
        if (searchParameters.getBookingType() == BookingType.SEAMEN && seamenErrorMessage == null) {
            airSolution.setSeamenHashMap(getFlightItineraryHashmap(seamenReply,office, true,searchParameters));
        }

        if (searchParameters.getBookingType() == BookingType.NON_MARINE && errorMessage == null) {
            airSolution.setNonSeamenHashMap(getFlightItineraryHashmap(fareMasterPricerTravelBoardSearchReply, office, false,searchParameters));
        }
        searchResponse.setAirSolution(airSolution);
        searchResponse.setProvider(provider());
        searchResponse.setFlightSearchOffice(office);
        
        long apiEndTime = System.currentTimeMillis();
        long apiDuration = apiEndTime - apiStartTime;
        String bookingType = searchParameters.getBookingType() == BookingType.SEAMEN ? "SEAMEN" : "NON_SEAMEN";
        logger.info("Split ticket - Amadeus API call completed at: {} (Duration: {} seconds) - Route: {} - BookingType: {}", 
                   new Date(apiEndTime), apiDuration/1000, route, bookingType);
        System.out.println("Split ticket - Amadeus API call completed at: " + new Date(apiEndTime) + 
                         " (Duration: " + apiDuration/1000 + " seconds) - Route: " + route + " - BookingType: " + bookingType);
        
        return searchResponse;
    }

    public String provider() {
        return "Amadeus";
    }
    @RetryOnFailure(attempts = 2, delay = 2000, exception = RetryException.class)
    public SearchResponse search(SearchParameters searchParameters, FlightSearchOffice office) throws Exception {
        String from = searchParameters.getJourneyList().get(0).getOrigin();
        String to = searchParameters.getJourneyList().get(searchParameters.getJourneyList().size()-1).getDestination();
        String route = from + " to " + to;
        
        long apiStartTime = System.currentTimeMillis();
        logger.debug("##################### search 186 AmadeusFlightSearch started for split  : ");
        logger.info("Split ticket - Amadeus API call started at: {} - Route: {}", new Date(apiStartTime), route);
        System.out.println("Split ticket - Amadeus API call started at: " + new Date(apiStartTime) + " - Route: " + route);
        logger.debug("##################### Original SearchParameters: \n"+ Json.toJson(searchParameters));
        
        SearchResponse searchResponse = new SearchResponse();
        AmadeusSessionWrapper amadeusSessionWrapper = null;
        searchResponse.setProvider("Amadeus");
        searchResponse.setFlightSearchOffice(office);
        searchResponse.setAirSegmentKey(from+to);
        FareMasterPricerTravelBoardSearchReply fareMasterPricerTravelBoardSearchReply = null;
        FareMasterPricerTravelBoardSearchReply seamenReply = null;

        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Start "+new Date());
            amadeusSessionWrapper = amadeusSessionManager.getSplitTicketSessionFast(office);
            System.out.println("End "+new Date());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Check if session is null or has null session ID, create new session if needed
            if (amadeusSessionWrapper == null || amadeusSessionWrapper.getmSession() == null || 
                amadeusSessionWrapper.getmSession().value == null || 
                amadeusSessionWrapper.getmSession().value.getSessionId() == null || 
                amadeusSessionWrapper.getmSession().value.getSessionId().isEmpty()) {
                
                logger.warn("Session is null or has null session ID, creating new session");
                amadeusSessionWrapper = amadeusSessionManager.createSession(office);
                
                if (amadeusSessionWrapper == null) {
                    logger.error("Failed to create new Amadeus session");
                    throw new Exception("Failed to create new Amadeus session");
                }
            }

            logger.debug("...................................Amadeus Search Session used: " + Json.toJson(amadeusSessionWrapper.getmSession().value));
            logger.debug("Execution time in getting session:: " + duration/1000 + " seconds");//to be removed
            if (searchParameters.getBookingType() == BookingType.SEAMEN) {
                seamenReply = serviceHandler.searchSplitAirlines(searchParameters, amadeusSessionWrapper,false);
                amadeusLogger.debug("AmadeusSearchRes "+ new Date()+" ------->>"+ new XStream().toXML(seamenReply));
            } else {
                fareMasterPricerTravelBoardSearchReply = serviceHandler.searchSplitAirlines(searchParameters, amadeusSessionWrapper,false);
                amadeusLogger.debug("AmadeusSearchRes "+ new Date()+" ------->>"+ new XStream().toXML(fareMasterPricerTravelBoardSearchReply));
            }
        } catch (ServerSOAPFaultException soapFaultException) {

            soapFaultException.printStackTrace();
            throw new IncompleteDetailsMessage(soapFaultException.getMessage(), soapFaultException.getCause());
        } catch (ClientTransportException clientTransportException) {
            //throw new IncompleteDetailsMessage(soapFaultException.getMessage(), soapFaultException.getCause());
            ErrorMessage errorMessage = ErrorMessageHelper.createErrorMessage("partialResults", ErrorMessage.ErrorType.ERROR, "Amadeus");
            searchResponse.getErrorMessageList().add(errorMessage);
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
            ErrorMessage errorMessage = ErrorMessageHelper.createErrorMessage("partialResults", ErrorMessage.ErrorType.ERROR, "Amadeus");
            searchResponse.getErrorMessageList().add(errorMessage);
            return searchResponse;
        }finally {
            updateSessionSafely(amadeusSessionWrapper);
        }

        FareMasterPricerTravelBoardSearchReply.ErrorMessage seamenErrorMessage = null;
        FareMasterPricerTravelBoardSearchReply.ErrorMessage errorMessage = null;
        if (seamenReply != null) {
            seamenErrorMessage = seamenReply.getErrorMessage();
            if(seamenErrorMessage != null)
                logger.debug("seamenErrorMessage :" + seamenErrorMessage.getErrorMessageText().getDescription() + "  officeId:"+ office.getOfficeId());
        }

        if (fareMasterPricerTravelBoardSearchReply != null) {
            errorMessage = fareMasterPricerTravelBoardSearchReply.getErrorMessage();
        }

        AirSolution airSolution = new AirSolution();
        logger.debug("#####################errorMessage is null");
        if (searchParameters.getBookingType() == BookingType.SEAMEN && seamenErrorMessage == null) {
            airSolution.setSeamenHashMap(getFlightItineraryHashmap(seamenReply,office, true,searchParameters));
        }

        if(searchParameters.getBookingType() == BookingType.NON_MARINE && errorMessage == null) {
            airSolution.setNonSeamenHashMap(getFlightItineraryHashmap(fareMasterPricerTravelBoardSearchReply, office,false,searchParameters));
        }
        searchResponse.setAirSolution(airSolution);
        searchResponse.setProvider(provider());
        searchResponse.setFlightSearchOffice(office);
        
        long apiEndTime = System.currentTimeMillis();
        long apiDuration = apiEndTime - apiStartTime;
        String bookingType = searchParameters.getBookingType() == BookingType.SEAMEN ? "SEAMEN" : "NON_SEAMEN";
        logger.info("Split ticket - Amadeus API call completed at: {} (Duration: {} seconds) - Route: {} - BookingType: {}", 
                   new Date(apiEndTime), apiDuration/1000, route, bookingType);
        System.out.println("Split ticket - Amadeus API call completed at: " + new Date(apiEndTime) + 
                         " (Duration: " + apiDuration/1000 + " seconds) - Route: " + route + " - BookingType: " + bookingType);
        
        return searchResponse;
    }

    private ConcurrentHashMap<Integer, FlightItinerary> getFlightItineraryHashmap(FareMasterPricerTravelBoardSearchReply fareMasterPricerTravelBoardSearchReply, FlightSearchOffice office, boolean isSeamen, SearchParameters searchParameters) {
        ConcurrentHashMap<Integer, FlightItinerary> flightItineraryHashMap = new ConcurrentHashMap<>();
        try{
            String currency = fareMasterPricerTravelBoardSearchReply.getConversionRate().getConversionRateDetail().get(0).getCurrency();
            List<FareMasterPricerTravelBoardSearchReply.FlightIndex> flightIndexList = fareMasterPricerTravelBoardSearchReply.getFlightIndex();
            List<FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp> baggageList = fareMasterPricerTravelBoardSearchReply.getServiceFeesGrp();
            FareMasterPricerTravelBoardSearchReply.MnrGrp mnrGrp = fareMasterPricerTravelBoardSearchReply.getMnrGrp();
            for (FareMasterPricerTravelBoardSearchReply.Recommendation recommendation : fareMasterPricerTravelBoardSearchReply.getRecommendation()) {
                for (ReferenceInfoType segmentRef : recommendation.getSegmentFlightRef()) {
                    FlightItinerary flightItinerary = new FlightItinerary();
                    flightItinerary.setPassportMandatory(false);
                    String validatingCarrierCode = null;
                    if (recommendation.getPaxFareProduct().get(0).getPaxFareDetail().getCodeShareDetails().get(0).getTransportStageQualifier().equals("V")) {
                        validatingCarrierCode = recommendation.getPaxFareProduct().get(0).getPaxFareDetail().getCodeShareDetails().get(0).getCompany();
                    }
                    flightItinerary.setPricingInformation(getPricingInformation(recommendation,searchParameters,isSeamen,mnrGrp,segmentRef,baggageList,validatingCarrierCode));
                    flightItinerary.getPricingInformation().setGdsCurrency(currency);
                    flightItinerary.getPricingInformation().setPricingOfficeId(office.getOfficeId());
                    List<String> contextList = getAvailabilityCtx(segmentRef, recommendation.getSpecificRecDetails());
                    flightItinerary = createJourneyInformation(segmentRef, flightItinerary, flightIndexList, recommendation, contextList,isSeamen);
                    flightItinerary.getPricingInformation().setPaxFareDetailsList(createFareDetails(recommendation, flightItinerary.getJourneyList()));
                    flightItineraryHashMap.put(flightItinerary.hashCode(), flightItinerary);
                }
            }
            return flightItineraryHashMap;
        }catch (Exception e){
            logger.debug("error in getFlightItineraryHashmap :"+ e.getMessage());
        }
        return flightItineraryHashMap;
    }

    private FlightItinerary createJourneyInformation(ReferenceInfoType segmentRef, FlightItinerary flightItinerary, List<FareMasterPricerTravelBoardSearchReply.FlightIndex> flightIndexList, FareMasterPricerTravelBoardSearchReply.Recommendation recommendation, List<String> contextList,boolean isSeamen){
        int flightIndexNumber = 0;
        int segmentIndex = 0;
        for(ReferencingDetailsType191583C referencingDetailsType : segmentRef.getReferencingDetail()) {
            //0 is for forward journey and refQualifier should be S for segment
            if (referencingDetailsType.getRefQualifier().equalsIgnoreCase("S") ) {
                Journey journey = new Journey();
                journey = setJourney(journey, flightIndexList.get(flightIndexNumber).getGroupOfFlights().get(referencingDetailsType.getRefNumber().intValue()-1),recommendation);
                journey.setSeamen(isSeamen);
                if(contextList.size() > 0 ){
                    setContextInformation(contextList, journey, segmentIndex);
                }
                flightItinerary.setFromLocation(journey.getFromLocation());
                flightItinerary.setToLocation(journey.getToLocation());
                flightItinerary.getJourneyList().add(journey);
                flightItinerary.getNonSeamenJourneyList().add(journey);
                ++flightIndexNumber;
            }


        }
        return flightItinerary;
    }


    private  void setContextInformation(List<String> contextList, Journey journey, int segmentIndex){
        for (AirSegmentInformation airSegmentInformation : journey.getAirSegmentList()){
            airSegmentInformation.setContextType(contextList.get(segmentIndex));
            segmentIndex = segmentIndex + 1;
        }
    }
    private Duration setTravelDuraion(String totalElapsedTime){
        String strHours = totalElapsedTime.substring(0, 2);
        String strMinutes = totalElapsedTime.substring(2);
        Duration duration = null;
        Integer hours = new Integer(strHours);
        int days = hours / 24;
        int dayHours = hours - (days * 24);
        try {
            duration = DatatypeFactory.newInstance().newDuration(true, 0, 0, days, dayHours, new Integer(strMinutes), 0);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return duration;
    }

    private Journey setJourney(Journey journey, FareMasterPricerTravelBoardSearchReply.FlightIndex.GroupOfFlights groupOfFlight, FareMasterPricerTravelBoardSearchReply.Recommendation recommendation){
        //no of stops
        journey.setNoOfStops(groupOfFlight.getFlightDetails().size()-1);

        //set travel time
        for(ProposedSegmentDetailsType proposedSegmentDetailsType : groupOfFlight.getPropFlightGrDetail().getFlightProposal()){
            if(proposedSegmentDetailsType.getUnitQualifier() != null && proposedSegmentDetailsType.getUnitQualifier().equals("EFT")){
                journey.setTravelTime(setTravelDuraion(proposedSegmentDetailsType.getRef()));
            }
        }
        //get farebases
        String fareBasis = getFareBasis(recommendation.getPaxFareProduct().get(0).getFareDetails().get(0));
        //set segments information

        String validatingCarrierCode = null;
        if(recommendation.getPaxFareProduct().get(0).getPaxFareDetail().getCodeShareDetails().get(0).getTransportStageQualifier().equals("V")) {
            validatingCarrierCode = recommendation.getPaxFareProduct().get(0).getPaxFareDetail().getCodeShareDetails().get(0).getCompany();
        }

        StringBuilder fullSegmentBuilder = new StringBuilder();
        for(FareMasterPricerTravelBoardSearchReply.FlightIndex.GroupOfFlights.FlightDetails flightDetails : groupOfFlight.getFlightDetails()){
            AirSegmentInformation airSegmentInformation = setSegmentInformation(flightDetails, fareBasis, validatingCarrierCode);
            if(airSegmentInformation.getToAirport().getAirportName() != null && airSegmentInformation.getFromAirport().getAirportName() != null) {
                journey.getAirSegmentList().add(airSegmentInformation);
                fullSegmentBuilder.append(airSegmentInformation.getFromLocation());
                fullSegmentBuilder.append(airSegmentInformation.getToLocation());
                journey.setProvider("Amadeus");
            }
        }
        List<AirSegmentInformation> airSegmentInformations = journey.getAirSegmentList();
        StringBuilder segmentBuilder = new StringBuilder();
        if(airSegmentInformations.size() > 0) {
            segmentBuilder.append(airSegmentInformations.get(0).getFromLocation());
            segmentBuilder.append(airSegmentInformations.get(airSegmentInformations.size()-1).getToLocation());
            journey.setFromLocation(airSegmentInformations.get(0).getFromLocation());
            journey.setToLocation(airSegmentInformations.get(airSegmentInformations.size()-1).getToLocation());
        }
        journey.setSegmentKey(segmentBuilder.toString());
        journey.setFullSegmentKey(fullSegmentBuilder.toString());
        getConnectionTime(journey.getAirSegmentList());
        return journey;
    }

    private void getConnectionTime(List<AirSegmentInformation> airSegments) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        if (airSegments.size() > 1) {
            for (int i = 1; i < airSegments.size(); i++) {
                Long arrivalTime;
                try {
                    arrivalTime = dateFormat.parse(
                            airSegments.get(i - 1).getArrivalTime()).getTime();

                    Long departureTime = dateFormat.parse(
                            airSegments.get(i).getDepartureTime()).getTime();
                    Long transit = departureTime - arrivalTime;
                    airSegments.get(i - 1).setConnectionTime(
                            Integer.valueOf((int) (transit / 60000)));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private AirSegmentInformation setSegmentInformation(FareMasterPricerTravelBoardSearchReply.FlightIndex.GroupOfFlights.FlightDetails flightDetails, String fareBasis, String validatingCarrierCode){
        AirSegmentInformation airSegmentInformation = new AirSegmentInformation();
        TravelProductType flightInformation=flightDetails.getFlightInformation();
        airSegmentInformation.setCarrierCode(flightInformation.getCompanyId().getMarketingCarrier());
        if(flightInformation.getCompanyId().getOperatingCarrier() != null)
            airSegmentInformation.setOperatingCarrierCode(flightInformation.getCompanyId().getOperatingCarrier());
        airSegmentInformation.setFlightNumber(flightInformation.getFlightOrtrainNumber());
        airSegmentInformation.setEquipment(flightInformation.getProductDetail().getEquipmentType());
        airSegmentInformation.setValidatingCarrierCode(validatingCarrierCode);
        //airSegmentInformation.setArrivalTime(flightInformation.getProductDateTime().getTimeOfArrival());
        //airSegmentInformation.setDepartureTime(flightInformation.getProductDateTime().getDateOfDeparture());
        airSegmentInformation.setFromTerminal(flightInformation.getLocation().get(0).getTerminal());
        airSegmentInformation.setToTerminal(flightInformation.getLocation().get(1).getTerminal());
        airSegmentInformation.setToDate(flightInformation.getProductDateTime().getDateOfDeparture());
        airSegmentInformation.setFromDate(flightInformation.getProductDateTime().getDateOfArrival());
        airSegmentInformation.setToLocation(flightInformation.getLocation().get(1).getLocationId());
        airSegmentInformation.setFromLocation(flightInformation.getLocation().get(0).getLocationId());
        Airport fromAirport = new Airport();
        Airport toAirport = new Airport();
        fromAirport = Airport.getAirport(airSegmentInformation.getFromLocation(), redisTemplate);
        toAirport = Airport.getAirport(airSegmentInformation.getToLocation(), redisTemplate);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmm");
        String DATE_FORMAT = "ddMMyyHHmm";
        DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT);
        DateTimeZone dateTimeZone = DateTimeZone.forID(fromAirport.getTime_zone());
        DateTime departureDate = DATETIME_FORMATTER.withZone(dateTimeZone).parseDateTime(flightInformation.getProductDateTime().getDateOfDeparture() + flightInformation.getProductDateTime().getTimeOfDeparture());
        dateTimeZone = DateTimeZone.forID(toAirport.getTime_zone());
        DateTime arrivalDate = DATETIME_FORMATTER.withZone(dateTimeZone).parseDateTime(flightInformation.getProductDateTime().getDateOfArrival() + flightInformation.getProductDateTime().getTimeOfArrival());

        airSegmentInformation.setDepartureDate(departureDate.toDate());
        airSegmentInformation.setDepartureTime(departureDate.toString());
        airSegmentInformation.setArrivalTime(arrivalDate.toString());
        airSegmentInformation.setArrivalDate(arrivalDate.toDate());
        airSegmentInformation.setFromAirport(fromAirport);
        airSegmentInformation.setToAirport(toAirport);
        Minutes diff = Minutes.minutesBetween(departureDate, arrivalDate);

        airSegmentInformation.setFareBasis(fareBasis);

        airSegmentInformation.setTravelTime("" + diff.getMinutes());
        if (flightInformation.getCompanyId() != null && flightInformation.getCompanyId().getMarketingCarrier() != null && flightInformation.getCompanyId().getMarketingCarrier().length() >= 2) {
            airSegmentInformation.setAirline(Airline.getAirlineByCode(flightInformation.getCompanyId().getMarketingCarrier(), redisTemplate));
            airSegmentInformation.setOperatingAirline(Airline.getAirlineByCode(flightInformation.getCompanyId().getOperatingCarrier(), redisTemplate));
        }

        //hopping
        if(flightDetails.getTechnicalStop()!=null){
            List<HoppingFlightInformation> hoppingFlightInformations = null;
            for (DateAndTimeInformationType dateAndTimeInformationType :flightDetails.getTechnicalStop()){
                //Arrival
                HoppingFlightInformation hop =new HoppingFlightInformation();
                hop.setLocation(dateAndTimeInformationType.getStopDetails().get(0).getLocationId());
                hop.setStartTime(new StringBuilder(dateAndTimeInformationType.getStopDetails().get(0).getFirstTime()).insert(2, ":").toString());
                SimpleDateFormat dateParser = new SimpleDateFormat("ddMMyy");
                Date startDate = null;
                Date endDate = null;
                try {
                    startDate = dateParser.parse(dateAndTimeInformationType.getStopDetails().get(0).getDate());
                    endDate = dateParser.parse(dateAndTimeInformationType.getStopDetails().get(1).getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                hop.setStartDate(dateFormat.format(startDate));
                //Departure
                hop.setEndTime(new StringBuilder(dateAndTimeInformationType.getStopDetails().get(1).getFirstTime()).insert(2, ":").toString());
                hop.setEndDate(dateFormat.format(endDate));
                if(hoppingFlightInformations==null){
                    hoppingFlightInformations = new ArrayList<HoppingFlightInformation>();
                }
                hoppingFlightInformations.add(hop);
            }
            airSegmentInformation.setHoppingFlightInformations(hoppingFlightInformations);
        }
        return airSegmentInformation;
    }

    private BigDecimal getFeeAmount(MonetaryInformationType174241S monInfo) {

        if (monInfo.getMonetaryDetails() != null && "BDT".equalsIgnoreCase(monInfo.getMonetaryDetails().getTypeQualifier())) {

            return monInfo.getMonetaryDetails().getAmount();
        } else if (monInfo.getOtherMonetaryDetails() != null) {

            return monInfo.getOtherMonetaryDetails().stream().filter(details -> "BDT".equalsIgnoreCase(details.getTypeQualifier())).map(MonetaryInformationDetailsType245528C::getAmount).findFirst().orElse(null);
        }

        return null;
    }
    private boolean isAllowed(List<StatusDetailsType256255C> statusInformation, String indicator, String action) {
        return statusInformation.stream().anyMatch(status -> indicator.equalsIgnoreCase(status.getIndicator()) && action.equalsIgnoreCase(status.getAction()));
    }
    private MnrSearchFareRules createSearchFareRules(ReferenceInfoType segmentRef, FareMasterPricerTravelBoardSearchReply.MnrGrp mnrGrp) {
        MnrSearchFareRules mnrSearchFareRules = new MnrSearchFareRules();

        try {
            BigDecimal changeFeeBeforeDeparture = null;
            BigDecimal cancellationFeeBeforeDeparture = null;
            Boolean isChangeAllowedBeforeDeparture = false;
            Boolean isCancellationAllowedBeforeDeparture = false;

            // Getting the reference Number here for 'M'
            String referenceNumber = segmentRef.getReferencingDetail().stream().filter(detail -> "M".equalsIgnoreCase(detail.getRefQualifier())).map(detail -> valueOf(detail.getRefNumber())).findFirst().orElse(null);


            if (referenceNumber == null) {
                return mnrSearchFareRules;
            }

            // Mapping Cancellation and Change here wrt to referenceNumber
            for (FareMasterPricerTravelBoardSearchReply.MnrGrp.MnrDetails mnrDetail : mnrGrp.getMnrDetails()) {
                if (!referenceNumber.equals(mnrDetail.getMnrRef().getItemNumberDetails().get(0).getNumber())) {
                    continue;
                }

                for (FareMasterPricerTravelBoardSearchReply.MnrGrp.MnrDetails.CatGrp catGrp : mnrDetail.getCatGrp()) {
                    BigInteger catRefNumber = catGrp.getCatInfo().getDescriptionInfo().getNumber();
                    List<StatusDetailsType256255C> statusInformation = catGrp.getStatusInfo().getStatusInformation();

                    // Change Fee (Category 31)
                    if (catRefNumber.equals(BigInteger.valueOf(31))) {

                        if (catGrp.getMonInfo() != null) {
                            MonetaryInformationType174241S monInfo = catGrp.getMonInfo();
                            changeFeeBeforeDeparture = (monInfo == null) ? null : getFeeAmount(monInfo);
                        }

                        isChangeAllowedBeforeDeparture = isAllowed(statusInformation, "BDJ", "1");
                    }

                    // Cancellation Fee (Category 33)
                    if (catRefNumber.equals(BigInteger.valueOf(33))) {

                        if (catGrp.getMonInfo() != null) {
                            MonetaryInformationType174241S monInfo = catGrp.getMonInfo();
                            cancellationFeeBeforeDeparture = (monInfo == null) ? null : getFeeAmount(monInfo);
                        }

                        isCancellationAllowedBeforeDeparture = isAllowed(statusInformation, "BDJ", "1");
                    }

                    if (changeFeeBeforeDeparture != null && cancellationFeeBeforeDeparture != null) {
                        break;
                    }
                }
                if (changeFeeBeforeDeparture != null && cancellationFeeBeforeDeparture != null) {
                    break;
                }
            }

            mnrSearchFareRules.setProvider(AMADEUS.toString());
            mnrSearchFareRules.setChangeFee(changeFeeBeforeDeparture);
            mnrSearchFareRules.setCancellationFee(cancellationFeeBeforeDeparture);
            mnrSearchFareRules.setChangeBeforeDepartureAllowed(isChangeAllowedBeforeDeparture);
            mnrSearchFareRules.setCancellationBeforeDepartureAllowed(isCancellationAllowedBeforeDeparture);

            return mnrSearchFareRules;
        } catch (Exception e) {
            logger.error("Mini rule error " + e.getMessage());
            return null;
        }
    }

    private PricingInformation getPricingInformation(FareMasterPricerTravelBoardSearchReply.Recommendation recommendation, SearchParameters searchParameters, boolean isSeamen,FareMasterPricerTravelBoardSearchReply.MnrGrp mnrGrp,ReferenceInfoType segmentRef,List<FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp> baggageList, String validatingCarrierCode) {
        PricingInformation pricingInformation = new PricingInformation();
        searchOfficeID = configurationMasterService.getConfig(ConfigMasterConstants.SPLIT_TICKET_AMADEUS_OFFICE_ID_GLOBAL.getKey());
        pricingInformation.setProvider("Amadeus");
        List<MonetaryInformationDetailsType> monetaryDetails = recommendation.getRecPriceInfo().getMonetaryDetail();
        BigDecimal totalFare = monetaryDetails.get(0).getAmount();
        BigDecimal totalTax = monetaryDetails.get(1).getAmount();
        pricingInformation.setBasePrice(totalFare.subtract(totalTax));
        pricingInformation.setTax(totalTax);
        pricingInformation.setTotalPrice(totalFare);
        pricingInformation.setTotalPriceValue(totalFare);
        pricingInformation.setPricingOfficeId(configurationMasterService.getConfig(ConfigMasterConstants.SPLIT_TICKET_AMADEUS_OFFICE_ID_GLOBAL.getKey()));
        List<PassengerTax> passengerTaxes= new ArrayList<>();
        for(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct paxFareProduct : recommendation.getPaxFareProduct()) {

            int paxCount = paxFareProduct.getPaxReference().get(0).getTraveller().size();
            int adtCount = searchParameters.getAdultCount();
            int chdCount = searchParameters.getChildCount();
            int infCount = searchParameters.getInfantCount();
            String paxType = paxFareProduct.getPaxReference().get(0).getPtc().get(0);
            PricingTicketingSubsequentType144401S fareDetails = paxFareProduct.getPaxFareDetail();
            BigDecimal amount = fareDetails.getTotalFareAmount();
            BigDecimal tax = fareDetails.getTotalTaxAmount();
            BigDecimal baseFare = amount.subtract(tax);
            if(paxType.equalsIgnoreCase("ADT") || paxType.equalsIgnoreCase("SEA")) {
//        		pricingInformation.setAdtBasePrice(baseFare.multiply(new BigDecimal(paxCount)));
                if (searchParameters.getAdultCount()>0 && isSeamen) {
                    PassengerTax passengerTax = new PassengerTax();
                    pricingInformation.setAdtBasePrice(baseFare);
                    pricingInformation.setAdtTotalPrice(amount);
                    passengerTax.setPassengerType("ADT");
                    passengerTax.setTotalTax(tax);
                    passengerTax.setPassengerCount(adtCount);
                    passengerTaxes.add(passengerTax);
                }
                if (searchParameters.getAdultCount()>0 && !isSeamen) {
                    PassengerTax passengerTax = new PassengerTax();
                    pricingInformation.setAdtBasePrice(baseFare);
                    pricingInformation.setAdtTotalPrice(amount);
                    passengerTax.setPassengerType("ADT");
                    passengerTax.setTotalTax(tax);
                    passengerTax.setPassengerCount(adtCount);
                    passengerTaxes.add(passengerTax);
                }
                if (searchParameters.getChildCount()>0 && isSeamen) {
                    PassengerTax passengerTax = new PassengerTax();
                    pricingInformation.setChdBasePrice(baseFare);
                    pricingInformation.setChdTotalPrice(amount);
                    passengerTax.setPassengerType("CHD");
                    passengerTax.setTotalTax(tax);
                    passengerTax.setPassengerCount(chdCount);
                    passengerTaxes.add(passengerTax);
                }
                if (searchParameters.getInfantCount()>0 && isSeamen) {
                    PassengerTax passengerTax = new PassengerTax();
                    pricingInformation.setInfBasePrice(baseFare);
                    pricingInformation.setInfTotalPrice(amount);
                    passengerTax.setPassengerType("INF");
                    passengerTax.setTotalTax(tax);
                    passengerTax.setPassengerCount(infCount);
                    passengerTaxes.add(passengerTax);
                }
            } else if(paxType.equalsIgnoreCase("CHD")) {
                PassengerTax passengerTax = new PassengerTax();
//				pricingInformation.setChdBasePrice(baseFare.multiply(new BigDecimal(paxCount)));
                pricingInformation.setChdBasePrice(baseFare);
                pricingInformation.setChdTotalPrice(amount);
                passengerTax.setPassengerType("CHD");
                passengerTax.setTotalTax(tax);
                passengerTax.setPassengerCount(chdCount);
                passengerTaxes.add(passengerTax);
            } else if(paxType.equalsIgnoreCase("INF")) {
                PassengerTax passengerTax = new PassengerTax();
//				pricingInformation.setInfBasePrice(baseFare.multiply(new BigDecimal(paxCount)));
                pricingInformation.setInfBasePrice(baseFare);
                pricingInformation.setInfTotalPrice(amount);
                passengerTax.setPassengerType("INF");
                passengerTax.setTotalTax(tax);
                passengerTax.setPassengerCount(infCount);
                passengerTaxes.add(passengerTax);
            }
            //passengerTaxes.add(passengerTax);
        }
        pricingInformation.setPassengerTaxes(passengerTaxes);
        if (!searchOfficeID.equalsIgnoreCase("BOMAK38SN")) {
            pricingInformation.setMnrSearchFareRules(createSearchFareRules(segmentRef, mnrGrp));
        }

        List<CabinDetails> cabinDetails = getCabinDetails(recommendation);

        if(!cabinDetails.isEmpty()) {
            pricingInformation.setCabinDetails(cabinDetails);
        }

        if (isSeamen) {
            PreloadedSeamanFareRules preloadedSeamanFareRules = PreloadedSeamanFareRules.findSeamanFareRuleByAirlineCode(validatingCarrierCode);
            if (preloadedSeamanFareRules != null) {
                pricingInformation.setPreloadedSeamanFareRules(preloadedSeamanFareRules);
            }
        }

        pricingInformation.setMnrSearchBaggage(createBaggageInformation(segmentRef, baggageList));
        return pricingInformation;
    }

    private List<CabinDetails> getCabinDetails(FareMasterPricerTravelBoardSearchReply.Recommendation recommendation) {

        List<CabinDetails> cabinDetailsList = new LinkedList<>();

        try {

            List<FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct> paxFareProductList = recommendation.getPaxFareProduct();

            if (paxFareProductList != null) {
                for (FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct paxFareProduct : paxFareProductList) {
                    List<FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails> fareDetailsList = paxFareProduct.getFareDetails();

                    if (fareDetailsList != null) {
                        for (FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails fareDetails : fareDetailsList) {
                            FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails.GroupOfFares groupOfFaresList = fareDetails.getGroupOfFares().get(0);

                            if (groupOfFaresList != null) {

                                CabinDetails cabinDetails = new CabinDetails();

                                FlightProductInformationType176659S flightProductInformation = groupOfFaresList.getProductInformation();

                                CabinProductDetailsType cabinProductDetails = flightProductInformation.getCabinProduct();

                                String cabin = cabinMap.get(cabinProductDetails.getCabin());
                                String bookingClass = cabinProductDetails.getRbd();
                                String availableSeats = cabinProductDetails.getAvlStatus();

                                cabinDetails.setCabin(cabin);
                                cabinDetails.setRbd(bookingClass);
                                cabinDetails.setAvailableSeats(availableSeats);

                                cabinDetailsList.add(cabinDetails);

                            }
                        }
                    }
                }
            }

            return cabinDetailsList;

        } catch (Exception e) {
            logger.debug("Error with getting cabin details from search recommendation {} : ", e.getMessage(), e);
            return null;
        }
    }

    private MnrSearchBaggage createBaggageInformation(ReferenceInfoType segmentRef, List<FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp> baggageListInfo) {

        try {
            MnrSearchBaggage mnrSearchBaggage = new MnrSearchBaggage();
            mnrSearchBaggage.setProvider(AMADEUS.toString());


            // Baggage reference number
            String baggageReferenceNumber = segmentRef.getReferencingDetail().stream().filter(referencingDetail -> "B".equalsIgnoreCase(referencingDetail.getRefQualifier())).map(referencingDetail -> valueOf(referencingDetail.getRefNumber())).findFirst().orElse(null);

            if (baggageReferenceNumber == null) {
                return mnrSearchBaggage;
            }


            // Finding the FBA reference from service group
            String fbaRefValue = null;
            outerForLoop:
            for (FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp serviceFeesGrp : baggageListInfo) {

                if (!"FBA".equalsIgnoreCase(serviceFeesGrp.getServiceTypeInfo().getCarrierFeeDetails().getType())) {
                    continue;
                }

                List<FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp.ServiceCoverageInfoGrp> serviceCoverageInfoGrpList = serviceFeesGrp.getServiceCoverageInfoGrp();
                for (FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp.ServiceCoverageInfoGrp serviceCoverageInfoGrp : serviceCoverageInfoGrpList) {
                    String serviceGroupRef = serviceCoverageInfoGrp.getItemNumberInfo().getItemNumber().getNumber();
                    if (!serviceGroupRef.equalsIgnoreCase(baggageReferenceNumber)) {
                        continue;
                    }

                    List<FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp.ServiceCoverageInfoGrp.ServiceCovInfoGrp> serviceCovInfoGrpList = serviceCoverageInfoGrp.getServiceCovInfoGrp();
                    for (FareMasterPricerTravelBoardSearchReply.ServiceFeesGrp.ServiceCoverageInfoGrp.ServiceCovInfoGrp serviceCovInfoGrp : serviceCovInfoGrpList) {

                        List<ReferencingDetailsType195561C> referencingDetailList = serviceCovInfoGrp.getRefInfo().getReferencingDetail();
                        for (ReferencingDetailsType195561C referencingDetails : referencingDetailList) {

                            if ("F".equalsIgnoreCase(referencingDetails.getRefQualifier())) {
                                fbaRefValue = String.valueOf(referencingDetails.getRefNumber());
                                break outerForLoop;
                            }
                        }
                    }
                }
            }

            // Find the baggage allowance info
            String finalFbaRefValue = fbaRefValue;
            String baggageAllowed = baggageListInfo.stream().filter(serviceFeesGrp -> serviceFeesGrp.getServiceTypeInfo().getCarrierFeeDetails().getType().equalsIgnoreCase("FBA")).flatMap(serviceFeesGrp -> serviceFeesGrp.getFreeBagAllowanceGrp().stream()).filter(freeBagAllowance -> freeBagAllowance.getItemNumberInfo().getItemNumberDetails().get(0).getNumber().toString().equals(finalFbaRefValue)).map(freeBagAllowance -> {
                BigInteger baggageValue = freeBagAllowance.getFreeBagAllownceInfo().getBaggageDetails().getFreeAllowance();
                String baggageUnit = freeBagAllowance.getFreeBagAllownceInfo().getBaggageDetails().getQuantityCode();
                return baggageValue + " " + MnrSearchBaggage.baggageCodes.get(baggageUnit);
            }).findFirst().orElse(null);

            mnrSearchBaggage.setAllowedBaggage(baggageAllowed);

            return mnrSearchBaggage;
        } catch (Exception e) {
            logger.debug("Error with baggage information at Search level", e);
            return null;
        }

    }

    public String getFareBasis(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails fareDetails){
        for(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails.GroupOfFares groupOfFares : fareDetails.getGroupOfFares()){
            return groupOfFares.getProductInformation().getFareProductDetail().getFareBasis();

        }
        return  null;
    }

    private List getAvailabilityCtx(ReferenceInfoType segmentRef, List<FareMasterPricerTravelBoardSearchReply.Recommendation.SpecificRecDetails> specificRecDetails){
        List<String> contextList = new ArrayList<>();
        for(ReferencingDetailsType191583C referencingDetailsType : segmentRef.getReferencingDetail()) {


            if(referencingDetailsType.getRefQualifier().equalsIgnoreCase("A")){
                BigInteger refNumber = referencingDetailsType.getRefNumber();
                for(FareMasterPricerTravelBoardSearchReply.Recommendation.SpecificRecDetails specificRecDetail : specificRecDetails){
                    if(refNumber.equals(specificRecDetail.getSpecificRecItem().getRefNumber())){
                        for(FareMasterPricerTravelBoardSearchReply.Recommendation.SpecificRecDetails.SpecificProductDetails specificProductDetails : specificRecDetail.getSpecificProductDetails()){
                            for(FareMasterPricerTravelBoardSearchReply.Recommendation.SpecificRecDetails.SpecificProductDetails.FareContextDetails fareContextDetails : specificProductDetails.getFareContextDetails()){
                                for(FareMasterPricerTravelBoardSearchReply.Recommendation.SpecificRecDetails.SpecificProductDetails.FareContextDetails.CnxContextDetails cnxContextDetails : fareContextDetails.getCnxContextDetails()){
                                    contextList.addAll(cnxContextDetails.getFareCnxInfo().getContextDetails().getAvailabilityCnxType());
                                }

                            }
                        }
                    }
                }
            }

        }

        return contextList;
    }

    private List<PAXFareDetails> createFareDetails(FareMasterPricerTravelBoardSearchReply.Recommendation recommendation, List<Journey> journeys){
        List<PAXFareDetails> paxFareDetailsList = new ArrayList<>();
        for(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct paxFareProduct :recommendation.getPaxFareProduct()){
            PAXFareDetails paxFareDetails = new PAXFareDetails();
            for(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails fareDetails :paxFareProduct.getFareDetails()){
                FareJourney fareJourney = new FareJourney();
                for(FareMasterPricerTravelBoardSearchReply.Recommendation.PaxFareProduct.FareDetails.GroupOfFares groupOfFares: fareDetails.getGroupOfFares()){
                    FareSegment fareSegment = new FareSegment();
                    fareSegment.setBookingClass(groupOfFares.getProductInformation().getCabinProduct().getRbd());
                    fareSegment.setCabinClass(groupOfFares.getProductInformation().getCabinProduct().getCabin());
                    paxFareDetails.setPassengerTypeCode(PassengerTypeCode.valueOf(groupOfFares.getProductInformation().getFareProductDetail().getPassengerType()));

                    fareSegment.setFareBasis(groupOfFares.getProductInformation().getFareProductDetail().getFareBasis());
                    fareJourney.getFareSegmentList().add(fareSegment);
                }
                paxFareDetails.getFareJourneyList().add(fareJourney);
            }
            paxFareDetailsList.add(paxFareDetails);
        }
        return paxFareDetailsList;
    }
}
