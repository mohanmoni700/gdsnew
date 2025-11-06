package services.QueueManagement;

import com.amadeus.xml.pnracc_14_1_1a.PNRReply;
import dto.queueManagement.*;


import java.util.List;
import java.util.Map;

public interface QueueManagementService {

    Map<String, PnrQueueDetailsDto> getQueueListFromRes(QueueListReqDTO queueListReqDTO);

    RemovePnrFromQueueRes removePnr (RemovePnrFomQueueDTO removePnrJson);

    PNRReply outwardMessageRequest(List<OutwardMessageDTO> outwardMessageList, String gdsPnr);

    List<OutwardMessageDTO> markOutwardMessagesStatus (List<OutwardMessageDTO> outwardMessageList,PNRReply pnrReply) ;

}
