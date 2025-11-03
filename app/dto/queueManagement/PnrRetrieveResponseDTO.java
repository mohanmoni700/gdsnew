package dto.queueManagement;

import java.util.List;


public class PnrRetrieveResponseDTO {

    private UpdatedJourneyDTO updatedJourney;

    private List<InwardMessageDTO> inwardMessages;

    public List<InwardMessageDTO> getInwardMessages() { return inwardMessages; }

    public void setInwardMessages(List<InwardMessageDTO> inwardMessages) { this.inwardMessages = inwardMessages; }

    public UpdatedJourneyDTO getUpdatedJourney() {
        return updatedJourney;
    }

    public void setUpdatedJourney(UpdatedJourneyDTO updatedJourney) {
        this.updatedJourney = updatedJourney;
    }

}
