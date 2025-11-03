package dto.queueManagement;

public class PnrQueueItemDto {

    private String gdsPnr;
    private String paxName;
    private String segments;
    private String travelDate;
    private String flightNumber;
    private String marketingCarrier;

    public String getGdsPnr() { return gdsPnr; }

    public void setGdsPnr(String gdsPnr) { this.gdsPnr = gdsPnr; }

    public String getPaxName() { return paxName; }

    public void setPaxName(String paxName) { this.paxName = paxName; }

    public String getSegments() { return segments; }

    public void setSegments(String segments) { this.segments = segments; }

    public String getTravelDate() { return travelDate; }

    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }

    public String getFlightNumber() { return flightNumber; }

    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getMarketingCarrier() { return marketingCarrier; }

    public void setMarketingCarrier(String marketingCarrier) { this.marketingCarrier = marketingCarrier; }

}
