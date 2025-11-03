package dto.queueManagement;

import java.util.List;

public class QueueListReqDTO {

    private String officeId;
    private List<QueueCategoryReqDTO> queueCategoryInfoList;

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public List<QueueCategoryReqDTO> getQueueCategoryInfoList() {
        return queueCategoryInfoList;
    }

    public void setQueueCategoryInfoList(List<QueueCategoryReqDTO> queueCategoryInfoList) {
        this.queueCategoryInfoList = queueCategoryInfoList;
    }



}
