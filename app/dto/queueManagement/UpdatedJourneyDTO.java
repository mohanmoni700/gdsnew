package dto.queueManagement;

import com.compassites.model.Journey;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatedJourneyDTO {

    private Map<Integer, String> segmentStatusMap;

    private List<Journey> cartJournies;

    private Map<String, List<String>> scheduleChange;

    public Map<Integer, String> getSegmentStatusMap() {
        return segmentStatusMap;
    }

    public void setSegmentStatusMap(Map<Integer, String> segmentStatusMap) {
        this.segmentStatusMap = segmentStatusMap;
    }

    public List<Journey> getCartJournies() {
        return cartJournies;
    }
    public void setCartJournies(List<Journey> cartJournies) {
        this.cartJournies = cartJournies;
    }
    public Map<String, List<String>> getScheduleChange() {
        return scheduleChange;
    }
    public void setScheduleChange(Map<String, List<String>> scheduleChange) {
        this.scheduleChange = scheduleChange;
    }

}
