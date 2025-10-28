package dto.queueManagement;

import java.math.BigInteger;
import java.util.List;

public class QueueCategoryReqDTO {

    private BigInteger queueNumber;
    private String queueType;
    private List<BigInteger> categoryNumbers;

    public BigInteger getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(BigInteger queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public List<BigInteger> getCategoryNumbers() {
        return categoryNumbers;
    }

    public void setCategoryNumbers(List<BigInteger> categoryNumbers) {
        this.categoryNumbers = categoryNumbers;
    }


}
