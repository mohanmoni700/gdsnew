package dto.queueManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutwardMessageDTO {

    private String paxRef;

    private String paxName;

    private String segment;

    private String segmentRef;

    private String carrierCode;

    private String remarkType;

    private List<String> freeText;

    private String status;

    private String errorMessage;

    private boolean existing;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    public String getPaxRef() {
        return paxRef;
    }

    public void setPaxRef(String paxRef) {
        this.paxRef = paxRef;
    }

    public String getPaxName() {
        return paxName;
    }

    public void setPaxName(String paxName) {
        this.paxName = paxName;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getSegmentRef() {
        return segmentRef;
    }

    public void setSegmentRef(String segmentRef) {
        this.segmentRef = segmentRef;
    }

    public String getRemarkType() {
        return remarkType;
    }

    public void setRemarkType(String remarkType) {
        this.remarkType = remarkType;
    }

    public List<String> getFreeText() {
        return freeText;
    }

    public void setFreeText(List<String> freeText) {
        this.freeText = freeText;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

}
