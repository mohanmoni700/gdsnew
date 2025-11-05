package dto.reissue;

public class AmadeusPaxRefAndTicket {

    private String paxRef;

    private String ticketNumber;
    private Long ticketId;

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    public String getPaxRef() {
        return paxRef;
    }

    public void setPaxRef(String paxRef) {
        this.paxRef = paxRef;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

}
