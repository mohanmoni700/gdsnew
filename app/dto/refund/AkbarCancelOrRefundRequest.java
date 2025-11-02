package dto.refund;

import java.util.List;
import java.util.Map;

public class AkbarCancelOrRefundRequest {

    private String tui;
    private Long transactionId;
    private Long crewOpId;
    private Map<String, List<SegmentDetails>> journeysToProcess;
    private boolean isPartial;
    private int adtCount;
    private int chdCount;
    private int infCount;
    private List<String> paxIDs;

    public List<String> getPaxIDs() {
        return paxIDs;
    }

    public void setPaxIDs(List<String> paxIDs) {
        this.paxIDs = paxIDs;
    }

    public int getAdtCount() {
        return adtCount;
    }

    public void setAdtCount(int adtCount) {
        this.adtCount = adtCount;
    }

    public int getChdCount() {
        return chdCount;
    }

    public void setChdCount(int chdCount) {
        this.chdCount = chdCount;
    }

    public int getInfCount() {
        return infCount;
    }

    public void setInfCount(int infCount) {
        this.infCount = infCount;
    }

    public String getTui() {
        return tui;
    }

    public void setTui(String tui) {
        this.tui = tui;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Map<String, List<SegmentDetails>> getJourneysToProcess() {
        return journeysToProcess;
    }

    public void setJourneysToProcess(Map<String, List<SegmentDetails>> journeysToProcess) {
        this.journeysToProcess = journeysToProcess;
    }

    public Long getCrewOpId() {
        return crewOpId;
    }

    public void setCrewOpId(Long crewOpId) {
        this.crewOpId = crewOpId;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    public static class SegmentDetails {

        private String crsPnr;
        private List<PaxIdAndTicket> paxIdAndTicketList;

        public String getCrsPnr() {
            return crsPnr;
        }

        public void setCrsPnr(String crsPnr) {
            this.crsPnr = crsPnr;
        }

        public List<PaxIdAndTicket> getPaxIdAndTicketList() {
            return paxIdAndTicketList;
        }

        public void setPaxIdAndTicketList(List<PaxIdAndTicket> paxIdAndTicketList) {
            this.paxIdAndTicketList = paxIdAndTicketList;
        }
    }

    public static class PaxIdAndTicket {

        private String paxId;
        private String ticketNumber;

        public String getPaxId() {
            return paxId;
        }

        public void setPaxId(String paxId) {
            this.paxId = paxId;
        }

        public String getTicketNumber() {
            return ticketNumber;
        }

        public void setTicketNumber(String ticketNumber) {
            this.ticketNumber = ticketNumber;
        }
    }

}
