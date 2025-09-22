package utils;

import com.amadeus.xml._2010._06.session_v3.Session;
import com.compassites.GDSWrapper.amadeus.ServiceHandler;
import com.compassites.constants.AmadeusConstants;
import models.AmadeusSessionWrapper;
import models.FlightSearchOffice;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import play.libs.Json;
import services.AmadeusSourceOfficeService;

import javax.xml.ws.Holder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Yaseen on 18-06-2015.
 */
@Service
public class AmadeusSessionManager {

    static org.slf4j.Logger logger = LoggerFactory.getLogger("gds");

    @Autowired
    private ServiceHandler serviceHandler;

    @Autowired
    private AmadeusSourceOfficeService sourceOfficeService;

    private boolean isValidSession(AmadeusSessionWrapper session) {
        if (session.isQueryInProgress()) {
            return false;
        }

        Period p = new Period(new DateTime(session.getLastQueryDate()), new DateTime(), PeriodType.minutes());
        int inactivityTimeInMinutes = p.getMinutes();

        if (inactivityTimeInMinutes >= AmadeusConstants.INACTIVITY_TIMEOUT) {
            session.delete();
            return false;
        }

        session.setQueryInProgress(true);
        session.setLastQueryDate(new Date());
        session.save();
        return true;
    }

    public AmadeusSessionWrapper createSession(FlightSearchOffice office) {
        logger.debug("Creating a  new session with office_id : {}", office.getOfficeId());
        try {
            AmadeusSessionWrapper amadeusSessionWrapper = serviceHandler.logIn(office, true);
            return createSessionWrapper(amadeusSessionWrapper, office);
        } catch (Exception e) {
            logger.error("Amadeus createSession error ", e);
            e.printStackTrace();
        }
        return null;
    }

    public AmadeusSessionWrapper getSession() throws Exception {
        FlightSearchOffice office = sourceOfficeService.getAllOffices().get(0);
        logger.debug("Default officeId used in getSession :{}", office.getOfficeId());
        return getSession(office);
    }

    public AmadeusSessionWrapper createSessionWrapper(Session session) {
        AmadeusSessionWrapper amadeusSessionWrapper = new AmadeusSessionWrapper();
        amadeusSessionWrapper.setActiveContext(false);
        amadeusSessionWrapper.setQueryInProgress(false);
        amadeusSessionWrapper.setLastQueryDate(new Date());
        amadeusSessionWrapper.setmSession(new Holder<>(session));
        amadeusSessionWrapper.save();
        return amadeusSessionWrapper;
    }

    public AmadeusSessionWrapper createSessionWrapper(AmadeusSessionWrapper amadeusSessionWrapper, FlightSearchOffice office) {

        amadeusSessionWrapper.setActiveContext(false);
        amadeusSessionWrapper.setQueryInProgress(false);
        amadeusSessionWrapper.setLastQueryDate(new Date());

        amadeusSessionWrapper.save();
        return amadeusSessionWrapper;

    }

    public void updateAmadeusSession(AmadeusSessionWrapper amadeusSessionWrapper) {
        if (amadeusSessionWrapper != null) {
            amadeusSessionWrapper.setQueryInProgress(false);
            amadeusSessionWrapper.update();
        }
    }

    public void safeUpdateAmadeusSession(AmadeusSessionWrapper amadeusSessionWrapper) {
        if (amadeusSessionWrapper != null) {
            try {
                amadeusSessionWrapper.setQueryInProgress(false);
                amadeusSessionWrapper.update();
            } catch (ClassCastException e) {
                logger.warn("ClassCastException occurred during session update, attempting workaround: {}", e.getMessage());
                System.out.println("ClassCastException occurred during session update, attempting workaround: " + e.getMessage());
                
                try {
                    // Workaround: Create a new instance and copy all data
                    AmadeusSessionWrapper newSession = new AmadeusSessionWrapper();
                    newSession.setSessionId(amadeusSessionWrapper.getSessionId());
                    newSession.setSecurityToken(amadeusSessionWrapper.getSecurityToken());
                    newSession.setSequenceNumber(amadeusSessionWrapper.getSequenceNumber());
                    newSession.setLastQueryDate(amadeusSessionWrapper.getLastQueryDate());
                    newSession.setQueryInProgress(false);
                    newSession.setActiveContext(amadeusSessionWrapper.isActiveContext());
                    newSession.setSessionUUID(amadeusSessionWrapper.getSessionUUID());
                    newSession.setGdsPNR(amadeusSessionWrapper.getGdsPNR());
                    newSession.setOfficeId(amadeusSessionWrapper.getOfficeId());
                    newSession.setPartnerName(amadeusSessionWrapper.getOfficeName());
                    newSession.setPartner(amadeusSessionWrapper.isPartner());
                    newSession.setmSession(amadeusSessionWrapper.getmSession());
                    newSession.setStateful(amadeusSessionWrapper.isStateful());
                    newSession.setSessionReUsed(amadeusSessionWrapper.isSessionReUsed());
                    newSession.save();
                    
                    logger.info("Successfully created new session instance as workaround for ClassCastException");
                    System.out.println("Successfully created new session instance as workaround for ClassCastException");
                } catch (Exception ex) {
                    logger.error("Failed to create new session instance as workaround: {}", ex.getMessage());
                    System.out.println("Failed to create new session instance as workaround: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                logger.error("Unexpected error during session update: {}", e.getMessage());
                System.out.println("Unexpected error during session update: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String storeActiveSession(AmadeusSessionWrapper amadeusSessionWrapper, String pnr) {

        String uuid = UUID.randomUUID().toString();
        amadeusSessionWrapper.setSessionUUID(uuid);
        amadeusSessionWrapper.setActiveContext(true);
        amadeusSessionWrapper.setSessionId(amadeusSessionWrapper.getSessionId());
        amadeusSessionWrapper.setSecurityToken(amadeusSessionWrapper.getSecurityToken());
        amadeusSessionWrapper.setGdsPNR(pnr);
        amadeusSessionWrapper.save();
        return uuid;

    }

    public Session getActiveSession(String sessionIdRef) {
        AmadeusSessionWrapper amadeusSessionWrapper = AmadeusSessionWrapper.findSessionByUUID(sessionIdRef);
        return amadeusSessionWrapper.getmSession().value;
    }

    public AmadeusSessionWrapper getActiveSessionByRef(String sessionIdRef) {
        AmadeusSessionWrapper amadeusSessionWrapper = AmadeusSessionWrapper.findSessionByUUID(sessionIdRef);
        amadeusSessionWrapper.setSessionReUsed(true);
        amadeusSessionWrapper.setStateful(true);
        return amadeusSessionWrapper;
    }

    public void removeActiveSession(Session session) {
        AmadeusSessionWrapper amadeusSessionWrapper = AmadeusSessionWrapper.findBySessionId(session.getSessionId());
        if (amadeusSessionWrapper != null) {
            amadeusSessionWrapper.delete();
        }
    }

    public AmadeusSessionWrapper getActiveSessionByGdsPNR(String pnr) {

        AmadeusSessionWrapper amadeusSessionWrapper =  AmadeusSessionWrapper.findByPNR(pnr);
        amadeusSessionWrapper.setSessionReUsed(true);
        amadeusSessionWrapper.setStateful(true);
        return amadeusSessionWrapper;

    }

    public synchronized AmadeusSessionWrapper getSession(FlightSearchOffice office) throws InterruptedException {
        return getSession(office, 0);
    }

    private synchronized AmadeusSessionWrapper getSession(FlightSearchOffice office, int recursionDepth) throws InterruptedException {
        logger.debug("AmadeusSessionManager getSession called, recursionDepth={}", recursionDepth," office id = {}", office.getOfficeId());
        List<AmadeusSessionWrapper> amadeusSessionWrapperList = AmadeusSessionWrapper.findAllInactiveContextListByOfficeId(office.getOfficeId());
        logger.info("amadeusSessionWrapperList size : {}" , amadeusSessionWrapperList.size()," json obj = {}", Json.toJson(amadeusSessionWrapperList));
        int count = 0;
        try {
            for (AmadeusSessionWrapper amadeusSessionWrapper : amadeusSessionWrapperList) {
                count++;
                if (amadeusSessionWrapper.isQueryInProgress()) {
                    continue;
                }
                Period p = new Period(new DateTime(amadeusSessionWrapper.getLastQueryDate()), new DateTime(), PeriodType.minutes());
                int inactivityTimeInMinutes = p.getMinutes();
                if (inactivityTimeInMinutes >= AmadeusConstants.INACTIVITY_TIMEOUT) {
                    amadeusSessionWrapper.delete();
                    continue;
                }
                amadeusSessionWrapper.setQueryInProgress(true);
                amadeusSessionWrapper.setLastQueryDate(new Date());
                amadeusSessionWrapper.save();
                if (amadeusSessionWrapper.getmSession() != null) {
                    logger.debug("Returning existing session .........................................{}", amadeusSessionWrapper.getmSession().value.getSessionId());
                    System.out.println("Returning existing session ........................................." + amadeusSessionWrapper.getmSession().value.getSessionId());
                }
                return amadeusSessionWrapper;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Amadeus getSession error ", e);
        }

        if (count >= AmadeusConstants.SESSION_POOL_SIZE) {
            logger.info("Session Pool Size reached : {}" , count);
            if (recursionDepth >= 2) {

                logger.error("Recursed Twice, Unsetting query In progress");
                for (AmadeusSessionWrapper amadeusSessionWrapper : amadeusSessionWrapperList) {

                    Period p = new Period(new DateTime(amadeusSessionWrapper.getLastQueryDate()), new DateTime(), PeriodType.minutes());
                    int inactivityTimeInMinutes = p.getMinutes();

                    if (amadeusSessionWrapper.isQueryInProgress() && inactivityTimeInMinutes >= AmadeusConstants.INACTIVITY_TIMEOUT) {
                        amadeusSessionWrapper.delete();
                        System.out.println("Unset Query In progress");
                        logger.debug("Unset Query In Progress {} ", amadeusSessionWrapper.getmSession().value.getSessionId());
                        continue;
                    }

                    amadeusSessionWrapper.setQueryInProgress(true);
                    amadeusSessionWrapper.setLastQueryDate(new Date());
                    amadeusSessionWrapper.save();
                    System.out.println("Returning existing session after 2 recursions........................................." + amadeusSessionWrapper.getmSession().value.getSessionId());
                    return amadeusSessionWrapper;
                }
            }
            logger.debug("Amadeus session pooling max connection size reached, waiting for connection...");
            Thread.sleep(2000);
            return getSession(office, recursionDepth + 1);
        } else {
            logger.debug("No existing session found, creating new session");
            return createSession(office);
        }
    }

    // ==================== SPLIT TICKET SPECIFIC SESSION METHODS ====================
    
    /**
     * Get session specifically for split ticket operations
     * Optimized for split ticket performance with dedicated session management
     */
    public synchronized AmadeusSessionWrapper getSplitTicketSessionFast(FlightSearchOffice office) throws InterruptedException {
        logger.debug("Split ticket - getSplitTicketSessionFast called for office: {}", office.getOfficeId());
        System.out.println("Split ticket - getSplitTicketSessionFast called for office: " + office.getOfficeId());
        
        try {
            // Get inactive sessions for this office
            List<AmadeusSessionWrapper> sessionList = AmadeusSessionWrapper.findAllInactiveContextListByOfficeId(office.getOfficeId());
            logger.debug("Split ticket - Found {} inactive sessions", sessionList.size());
            
            // Try to find an available session
            for (AmadeusSessionWrapper session : sessionList) {
                if (!session.isQueryInProgress()) {
                    // Check if session is not expired
                    Period p = new Period(new DateTime(session.getLastQueryDate()), new DateTime(), PeriodType.minutes());
                    int inactivityTimeInMinutes = p.getMinutes();
                    
                    if (inactivityTimeInMinutes < AmadeusConstants.INACTIVITY_TIMEOUT) {
                        // Mark session as in progress
                        session.setQueryInProgress(true);
                        session.setLastQueryDate(new Date());
                        session.save();
                        
                        logger.debug("Split ticket - Returning existing session: {}", 
                                   session.getmSession() != null ? session.getmSession().value.getSessionId() : "null");
                        System.out.println("Split ticket - Returning existing session: " + 
                                         (session.getmSession() != null ? session.getmSession().value.getSessionId() : "null"));
                        return session;
                    } else {
                        // Session expired, delete it
                        try {
                            session.delete();
                        } catch (Exception e) {
                            logger.warn("Split ticket - Failed to delete expired session: {}", e.getMessage());
                        }
                    }
                }
            }
            
            // No available session found, create new one
            logger.debug("Split ticket - No available session found, creating new session");
            System.out.println("Split ticket - No available session found, creating new session");
            return createSplitTicketSessionFast(office);
            
        } catch (Exception e) {
            logger.error("Split ticket - Error in getSplitTicketSessionFast: {}", e.getMessage());
            System.out.println("Split ticket - Error in getSplitTicketSessionFast: " + e.getMessage());
            e.printStackTrace();
            return createSplitTicketSessionFast(office);
        }
    }
    
    /**
     * Create new session specifically for split ticket operations
     */
    private AmadeusSessionWrapper createSplitTicketSessionFast(FlightSearchOffice office) {
        logger.debug("Split ticket - createSplitTicketSessionFast called for office: {}", office.getOfficeId());
        System.out.println("Split ticket - createSplitTicketSessionFast called for office: " + office.getOfficeId());
        
        try {
            AmadeusSessionWrapper newSession = new AmadeusSessionWrapper();
            newSession.setOfficeId(office.getOfficeId());
            newSession.setPartner(office.isPartner());
            newSession.setQueryInProgress(true);
            newSession.setActiveContext(false);
            newSession.setLastQueryDate(new Date());
            newSession.setStateful(false);
            newSession.setSessionReUsed(false);
            
            // Create new Amadeus session
            AmadeusSessionWrapper sessionReply = createSession(office);
            if (sessionReply != null && sessionReply.getSessionId() != null) {
                newSession.setSessionId(sessionReply.getSessionId());
                newSession.setSecurityToken(sessionReply.getSecurityToken());
                newSession.setSequenceNumber(sessionReply.getSequenceNumber());
                newSession.setmSession(sessionReply.getmSession());
                
                newSession.save();
                
                logger.info("Split ticket - Created new session: {}", sessionReply.getSessionId());
                System.out.println("Split ticket - Created new session: " + sessionReply.getSessionId());
                return newSession;
            } else {
                logger.error("Split ticket - Failed to create Amadeus session");
                System.out.println("Split ticket - Failed to create Amadeus session");
                return null;
            }
        } catch (Exception e) {
            logger.error("Split ticket - Error creating new session: {}", e.getMessage());
            System.out.println("Split ticket - Error creating new session: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Release session specifically for split ticket operations
     * Uses safe update to handle ClassCastException
     */
    public void releaseSplitTicketSessionFast(AmadeusSessionWrapper amadeusSessionWrapper) {
        if (amadeusSessionWrapper != null) {
            logger.debug("Split ticket - releaseSplitTicketSessionFast called");
            System.out.println("Split ticket - releaseSplitTicketSessionFast called");
            
            try {
                // Try normal update first
                amadeusSessionWrapper.setQueryInProgress(false);
                amadeusSessionWrapper.update();
                logger.debug("Split ticket - Session released successfully");
                System.out.println("Split ticket - Session released successfully");
            } catch (ClassCastException e) {
                logger.warn("Split ticket - ClassCastException during session release, using workaround: {}", e.getMessage());
                System.out.println("Split ticket - ClassCastException during session release, using workaround: " + e.getMessage());
                
                // Use workaround for ClassCastException
                try {
                    AmadeusSessionWrapper newSession = new AmadeusSessionWrapper();
                    newSession.setSessionId(amadeusSessionWrapper.getSessionId());
                    newSession.setSecurityToken(amadeusSessionWrapper.getSecurityToken());
                    newSession.setSequenceNumber(amadeusSessionWrapper.getSequenceNumber());
                    newSession.setLastQueryDate(new Date());
                    newSession.setQueryInProgress(false);
                    newSession.setActiveContext(amadeusSessionWrapper.isActiveContext());
                    newSession.setSessionUUID(amadeusSessionWrapper.getSessionUUID());
                    newSession.setGdsPNR(amadeusSessionWrapper.getGdsPNR());
                    newSession.setOfficeId(amadeusSessionWrapper.getOfficeId());
                    newSession.setPartnerName(amadeusSessionWrapper.getOfficeName());
                    newSession.setPartner(amadeusSessionWrapper.isPartner());
                    newSession.setmSession(amadeusSessionWrapper.getmSession());
                    newSession.setStateful(amadeusSessionWrapper.isStateful());
                    newSession.setSessionReUsed(amadeusSessionWrapper.isSessionReUsed());
                    newSession.save();
                    
                    logger.info("Split ticket - Session released using workaround");
                    System.out.println("Split ticket - Session released using workaround");
                } catch (Exception ex) {
                    logger.error("Split ticket - Failed to release session with workaround: {}", ex.getMessage());
                    System.out.println("Split ticket - Failed to release session with workaround: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                logger.error("Split ticket - Unexpected error during session release: {}", e.getMessage());
                System.out.println("Split ticket - Unexpected error during session release: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Update session specifically for split ticket operations
     * Uses safe update to handle ClassCastException
     */
    public void updateSplitTicketSessionFast(AmadeusSessionWrapper amadeusSessionWrapper) {
        if (amadeusSessionWrapper != null) {
            logger.debug("Split ticket - updateSplitTicketSessionFast called");
            System.out.println("Split ticket - updateSplitTicketSessionFast called");
            
            try {
                amadeusSessionWrapper.setQueryInProgress(false);
                amadeusSessionWrapper.update();
                logger.debug("Split ticket - Session updated successfully");
                System.out.println("Split ticket - Session updated successfully");
            } catch (ClassCastException e) {
                logger.warn("Split ticket - ClassCastException during session update, using workaround: {}", e.getMessage());
                System.out.println("Split ticket - ClassCastException during session update, using workaround: " + e.getMessage());
                
                // Use workaround for ClassCastException
                try {
                    AmadeusSessionWrapper newSession = new AmadeusSessionWrapper();
                    newSession.setSessionId(amadeusSessionWrapper.getSessionId());
                    newSession.setSecurityToken(amadeusSessionWrapper.getSecurityToken());
                    newSession.setSequenceNumber(amadeusSessionWrapper.getSequenceNumber());
                    newSession.setLastQueryDate(new Date());
                    newSession.setQueryInProgress(false);
                    newSession.setActiveContext(amadeusSessionWrapper.isActiveContext());
                    newSession.setSessionUUID(amadeusSessionWrapper.getSessionUUID());
                    newSession.setGdsPNR(amadeusSessionWrapper.getGdsPNR());
                    newSession.setOfficeId(amadeusSessionWrapper.getOfficeId());
                    newSession.setPartnerName(amadeusSessionWrapper.getOfficeName());
                    newSession.setPartner(amadeusSessionWrapper.isPartner());
                    newSession.setmSession(amadeusSessionWrapper.getmSession());
                    newSession.setStateful(amadeusSessionWrapper.isStateful());
                    newSession.setSessionReUsed(amadeusSessionWrapper.isSessionReUsed());
                    newSession.save();
                    
                    logger.info("Split ticket - Session updated using workaround");
                    System.out.println("Split ticket - Session updated using workaround");
                } catch (Exception ex) {
                    logger.error("Split ticket - Failed to update session with workaround: {}", ex.getMessage());
                    System.out.println("Split ticket - Failed to update session with workaround: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                logger.error("Split ticket - Unexpected error during session update: {}", e.getMessage());
                System.out.println("Split ticket - Unexpected error during session update: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
