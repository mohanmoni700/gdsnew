package dto.queueManagement;

import java.util.List;

public class QueueListResDTO {

    private String pnrCount;
    private List<PnrQueueDetailsDto> pnrQueueDetailsDtoList;

    public String getPnrCount() {
        return pnrCount;
    }

    public void setPnrCount(String pnrCount) {
        this.pnrCount = pnrCount;
    }

    public List<PnrQueueDetailsDto> getPnrQueueDetailsDtoList() {
        return pnrQueueDetailsDtoList;
    }

    public void setPnrQueueDetailsDtoList(List<PnrQueueDetailsDto> pnrQueueDetailsDtoList) {
        this.pnrQueueDetailsDtoList = pnrQueueDetailsDtoList;
    }
}
