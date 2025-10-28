package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentRefDTO {

    private String segmentRefNo;

    private String lineNumber;

    private List<String> segmentStatus;


    public String getSegmentRefNo() {
        return segmentRefNo;
    }

    public void setSegmentRefNo(String segmentRefNo) {
        this.segmentRefNo = segmentRefNo;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<String> getSegmentStatus() {
        return segmentStatus;
    }

    public void setSegmentStatus(List<String> segmentStatus) {
        this.segmentStatus = segmentStatus;
    }
}
