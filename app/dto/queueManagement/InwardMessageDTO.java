package dto.queueManagement;

import java.util.List;

public class InwardMessageDTO {

    private String segmentName;
    private String companyId;
    private String type;
    private List<String> messages;

    public String getSegmentName() { return segmentName; }

    public void setSegmentName(String segmentName) { this.segmentName = segmentName; }

    public String getCompanyId() { return companyId; }

    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public List<String> getMessages() { return messages; }

    public void setMessages(List<String> messages) { this.messages = messages; }


}
