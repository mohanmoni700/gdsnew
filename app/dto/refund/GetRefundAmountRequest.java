package dto.refund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetRefundAmountRequest {

    private String tui;
    private Long transactionId;
    private int adtCount;
    private int chdCount;
    private int infCount;
    private boolean isPartial;
    private Long crewOpId;

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

    public boolean isPartial() {
        return isPartial;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    public Long getCrewOpId() {
        return crewOpId;
    }

    public void setCrewOpId(Long crewOpId) {
        this.crewOpId = crewOpId;
    }

}
