package services.QueueManagement;

import dto.queueManagement.PnrQueueDetailsDto;
import dto.queueManagement.QueueListReqDTO;
import dto.queueManagement.RemovePnrFomQueueDTO;
import dto.queueManagement.RemovePnrFromQueueRes;


import java.util.Map;

public interface QueueManagementService {

    Map<String, PnrQueueDetailsDto> getQueueListFromRes(QueueListReqDTO queueListReqDTO);

    RemovePnrFromQueueRes removePnr (RemovePnrFomQueueDTO removePnrJson);

}
