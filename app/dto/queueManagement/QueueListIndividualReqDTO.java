package dto.queueManagement;

import java.math.BigInteger;

public class QueueListIndividualReqDTO {

    private String officeId;

    private BigInteger queueNumber;

    private BigInteger categoryNumber;

    private BigInteger dateRange;

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public BigInteger getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(BigInteger queueNumber) {
        this.queueNumber = queueNumber;
    }

    public BigInteger getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(BigInteger categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public BigInteger getDateRange() {
        return dateRange;
    }

    public void setDateRange(BigInteger dateRange) {
        this.dateRange = dateRange;
    }
}