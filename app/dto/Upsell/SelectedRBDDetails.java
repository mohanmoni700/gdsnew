package dto.Upsell;

import com.compassites.model.Journey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectedRBDDetails {
    private String segmentOrigin;
    private String segmentDestination;
    private String cabinClass;
    private String bookingClass;
    private int availableSeats;
    private String departureTime;

    public String getSegmentOrigin() {
        return segmentOrigin;
    }

    public void setSegmentOrigin(String segmentOrigin) {
        this.segmentOrigin = segmentOrigin;
    }

    public String getSegmentDestination() {
        return segmentDestination;
    }

    public void setSegmentDestination(String segmentDestination) {
        this.segmentDestination = segmentDestination;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getBookingClass() {
        return bookingClass;
    }

    public void setBookingClass(String bookingClass) {
        this.bookingClass = bookingClass;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}
