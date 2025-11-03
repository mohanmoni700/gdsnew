package dto.queueManagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigInteger;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PnrQueueDetailsDto {

    private String pnrCount;
    private BigInteger queueNumber;
    private BigInteger categoryNumber;
    private BigInteger dateRange;
    private List<PnrQueueItemDto> items;

    public String getPnrCount() {
        return pnrCount;
    }

    public void setPnrCount(String pnrCount) {
        this.pnrCount = pnrCount;
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

    public List<PnrQueueItemDto> getItems() {
        return items;
    }

    public void setItems(List<PnrQueueItemDto> items) {
        this.items = items;
    }
}
