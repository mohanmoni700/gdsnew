package dto;


import com.compassites.model.FareJourney;
import com.compassites.model.FareSegment;
import com.compassites.model.PAXFareDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import services.AmadeusFlightSearch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CabinDetails implements java.io.Serializable{

    private String rbd;
    private String cabin;
    private String availableSeats;


    public String getRbd() {
        return rbd;
    }

    public void setRbd(String rbd) {
        this.rbd = rbd;
    }

    public String getCabin() {
        return cabin;
    }

    public void setCabin(String cabin) {
        this.cabin = cabin;
    }

    public String getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(String availableSeats) {
        this.availableSeats = availableSeats;
    }


    public  static List<CabinDetails> getCabinDetailsFromPaxFareDetails(List<PAXFareDetails> paxFareDetailsList) {
        List<CabinDetails> cabinDetailsList = new LinkedList<>();

        try {
            if (paxFareDetailsList != null) {
                for (PAXFareDetails paxFareDetail : paxFareDetailsList) {
                    List<FareJourney> fareJourneyList = paxFareDetail.getFareJourneyList();

                    if (fareJourneyList != null) {
                        for (FareJourney fareJourney : fareJourneyList) {
                            List<FareSegment> fareSegmentList = fareJourney.getFareSegmentList();

                            if (fareSegmentList != null) {
                                for (FareSegment fareSegment : fareSegmentList) {
                                    CabinDetails cabinDetails = new CabinDetails();

                                    String cabin = cabinMap.get(fareSegment.getCabinClass());
                                    String bookingClass = fareSegment.getBookingClass();
                                    String availableSeats = fareSegment.getAvailableSeats();

                                    cabinDetails.setCabin(cabin);
                                    cabinDetails.setRbd(bookingClass);
                                    cabinDetails.setAvailableSeats(availableSeats);

                                    cabinDetailsList.add(cabinDetails);
                                }
                            }
                        }
                    }
                }
            }

            return cabinDetailsList;

        } catch (Exception e) {
            return null;
        }
    }

    public static final Map<String,String> cabinMap = new HashMap<>();

    static {
        cabinMap.put("C","BUSINESS");
        cabinMap.put("F","FIRST");
        cabinMap.put("Y","ECONOMIC");
        cabinMap.put("W","ECONOMIC PREMIUM");
        cabinMap.put("M","ECONOMIC STANDARD");
    }

}
