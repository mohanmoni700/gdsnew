package services.QueueManagement;

import com.amadeus.xml.pnracc_14_1_1a.LongFreeTextType;
import com.amadeus.xml.pnracc_14_1_1a.PNRReply;
import com.amadeus.xml.pnracc_14_1_1a.SpecialRequirementsTypeDetailsTypeI;
import com.amadeus.xml.qdqlrr_11_1_1a.*;
import com.amadeus.xml.quqmdr_03_1_1a.ApplicationErrorInformationTypeI;
import com.amadeus.xml.quqmdr_03_1_1a.FreeTextInformationType;
import com.amadeus.xml.quqmdr_03_1_1a.QueueRemoveItemReply;
import com.compassites.GDSWrapper.amadeus.ServiceHandler;
import com.compassites.model.ErrorMessage;
import dto.queueManagement.*;
import models.AmadeusSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class QueueManagementServiceImpl implements QueueManagementService{

    @Autowired
    private ServiceHandler serviceHandler;

    private static final Logger logger = LoggerFactory.getLogger("gds");

    @Override
    public Map<String, PnrQueueDetailsDto> getQueueListFromRes(QueueListReqDTO queueListReqDTO) {

        AmadeusSessionWrapper amadeusSessionWrapper = null;
        ServiceHandler serviceHandler = null;
        Map<String, QueueListReply> responseMap = new LinkedHashMap<>();

        try {
            serviceHandler = new ServiceHandler();
            amadeusSessionWrapper = serviceHandler.logIn(queueListReqDTO.getOfficeId(),false);

            List<QueueCategoryReqDTO> queueCategoryInfoList = queueListReqDTO.getQueueCategoryInfoList();

            if (queueCategoryInfoList == null || queueCategoryInfoList.isEmpty()) {
                logger.warn("No queue category info found in request");
                return null;
            }

            logger.info("Processing {} queues", queueCategoryInfoList.size());

            // Looping through each queue-category combination
            for (QueueCategoryReqDTO queueInfo : queueCategoryInfoList) {

                BigInteger queueNumber = queueInfo.getQueueNumber();
                String queueType = queueInfo.getQueueType();
                List<BigInteger> categoryNumbers = queueInfo.getCategoryNumbers();

                logger.debug("Processing Queue: {}, Type: {}, Categories: {}", queueNumber, queueType, categoryNumbers.size());

                for (BigInteger categoryNumber : categoryNumbers) {

                    if ("DUAL".equalsIgnoreCase(queueType)) {
                        // date ranges (1 to 4)
                        for (int dateRange = 1; dateRange <= 4; dateRange++) {

                            QueueListReply reply = executeQueueListRequest(serviceHandler, amadeusSessionWrapper, queueListReqDTO.getOfficeId(), queueNumber, categoryNumber, BigInteger.valueOf(dateRange));

                            if (reply != null) {
                                String key = queueNumber + "-" + categoryNumber + "-" + dateRange;
                                responseMap.put(key, reply);
                                logger.debug("Stored response for key {}", key);
                            }
                        }
                    } else {

                        QueueListReply reply = executeQueueListRequest(serviceHandler, amadeusSessionWrapper, queueListReqDTO.getOfficeId(), queueNumber, categoryNumber, null);

                        if (reply != null) {
                            String key = queueNumber + "-" + categoryNumber + "-0";
                            responseMap.put(key, reply);
                            logger.debug("Stored response for key without date range {}", key);
                        }
                    }
                }
            }

            logger.info("Total queue list requests executed: {}", responseMap.size());

            Map<String, PnrQueueDetailsDto> queueDetailResponseMap = getDetailsFromQueueListReply(responseMap);

            return queueDetailResponseMap;

        } catch (Exception e) {
            logger.error("Error getting queue list: {}", e.getMessage(), e);
            return null;
        }
    }

    private QueueListReply executeQueueListRequest(ServiceHandler serviceHandler, AmadeusSessionWrapper amadeusSessionWrapper, String officeId, BigInteger queueNumber, BigInteger categoryNumber, BigInteger dateRange) {

        try {

            QueueListIndividualReqDTO individualRequest = new QueueListIndividualReqDTO();
            individualRequest.setOfficeId(officeId);
            individualRequest.setQueueNumber(queueNumber);
            individualRequest.setCategoryNumber(categoryNumber);
            individualRequest.setDateRange(dateRange);

            logger.debug("Executing queue list request - Queue: {}, Category: {}, DateRange: {}", queueNumber, categoryNumber, dateRange);

            QueueListReply queueListReply = serviceHandler.queueListReq(individualRequest, amadeusSessionWrapper);

            return queueListReply;

        } catch (Exception e) {
            logger.error("Error executing queue list request for Queue: {}, Category: {}, DateRange: {}", queueNumber, categoryNumber, dateRange, e);
            return null;
        }
    }

    private Map<String, PnrQueueDetailsDto> getDetailsFromQueueListReply(Map<String, QueueListReply> responseMap) {

        Map<String, PnrQueueDetailsDto> detailedResponseMap = new LinkedHashMap<>();
        BigInteger queueNumber = null;
        String categoryNumber = null;
        BigInteger dateRange = null;
        BigInteger queuePnrCount = null;
        String origin = " ";
        String destination = " ";

        try {
            for (Map.Entry<String, QueueListReply> entry : responseMap.entrySet()) {
                String key = entry.getKey();
                QueueListReply queueListReply = entry.getValue();

                if (queueListReply != null && queueListReply.getQueueView() != null) {
                    QueueListReply.QueueView queueView = queueListReply.getQueueView();

                    if (queueView.getQueueNumber() != null && queueView.getQueueNumber().getQueueDetails() != null) {
                        queueNumber = queueView.getQueueNumber().getQueueDetails().getNumber();
                    }

                    if (queueView.getCategoryDetails() != null && queueView.getCategoryDetails().getSubQueueInfoDetails() != null) {
                        categoryNumber = queueView.getCategoryDetails().getSubQueueInfoDetails().getItemNumber();
                    }

                    if (queueView.getDate() != null) {
                        dateRange = queueView.getDate().getTimeMode();
                    }

                    List<NumberOfUnitsType> pnrCountList = queueView.getPnrCount();
                    if (pnrCountList != null && !pnrCountList.isEmpty() && pnrCountList.get(0).getQuantityDetails() != null) {
                        queuePnrCount = pnrCountList.get(0).getQuantityDetails().getNumberOfUnit();
                    }

                    List<PnrQueueItemDto> itemDtos = new ArrayList<>();
                    List<QueueListReply.QueueView.Item> itemList = queueView.getItem();

                    if (itemList != null) {
                        for (QueueListReply.QueueView.Item item : itemList) {
                            if (item != null) {
                                PnrQueueItemDto dto = new PnrQueueItemDto();

                                if (item.getPaxName() != null && item.getPaxName().getPaxDetails() != null)
                                    dto.setPaxName(item.getPaxName().getPaxDetails().getSurname());

                                if (item.getRecLoc() != null && item.getRecLoc().getReservation() != null)
                                    dto.setGdsPnr(item.getRecLoc().getReservation().getControlNumber());

                                TravelProductInformationTypeI segment = item.getSegment();
                                if (segment != null) {
                                    origin = segment.getBoardPointDetails() != null ? segment.getBoardPointDetails().getTrueLocation() : null;
                                    destination = segment.getOffpointDetails() != null ? segment.getOffpointDetails().getTrueLocation() : null;

                                    dto.setSegments(origin + "-" + destination);

                                    dto.setMarketingCarrier(segment.getCompanyDetails() != null ? segment.getCompanyDetails().getMarketingCompany() : null);
                                    dto.setFlightNumber(segment.getFlightIdentification() != null ? segment.getFlightIdentification().getFlightNumber() : null);
                                    dto.setTravelDate(segment.getFlightDate() != null ? segment.getFlightDate().getDepartureDate() : null);
                                }

                                itemDtos.add(dto);
                            }
                        }
                    }

                    PnrQueueDetailsDto queueDto = new PnrQueueDetailsDto();
                    queueDto.setPnrCount(queuePnrCount != null ? queuePnrCount.toString() : null);
                    queueDto.setQueueNumber(queueNumber);
                    queueDto.setCategoryNumber(new BigInteger(categoryNumber));
                    queueDto.setDateRange(dateRange);
                    queueDto.setItems(itemDtos);

                    detailedResponseMap.put(key, queueDto);
                }
            }
        } catch (Exception e) {
            logger.error("Error getting details from the queue list reply", e);
        }

        return detailedResponseMap;
    }

    @Override
    public RemovePnrFromQueueRes removePnr (RemovePnrFomQueueDTO removePnrJson) {

        AmadeusSessionWrapper amadeusSessionWrapper = null;
        ServiceHandler serviceHandler = null;
        RemovePnrFromQueueRes removePnrFromQueueRes = new RemovePnrFromQueueRes();

        try {
            serviceHandler = new ServiceHandler();
            // Recheck the office id
            amadeusSessionWrapper = serviceHandler.logIn(removePnrJson.getIata(),false);
            QueueRemoveItemReply queueRemoveItemReply = serviceHandler.queueRemoveItem(amadeusSessionWrapper,removePnrJson);

            if (queueRemoveItemReply != null && queueRemoveItemReply.getGoodResponse() != null && queueRemoveItemReply.getErrorReturn() == null) {
                removePnrFromQueueRes.setSuccess(true);
            } else {
                removePnrFromQueueRes.setSuccess(false);
                QueueRemoveItemReply.ErrorReturn errorReturn = queueRemoveItemReply.getErrorReturn();
                String errorCode = null;
                List<String> errorTextList = null;

                if (errorReturn != null) {
                    ApplicationErrorInformationTypeI errorDefinition = errorReturn.getErrorDefinition();
                    if (errorDefinition != null && errorDefinition.getErrorDetails() != null) {
                        errorCode = errorDefinition.getErrorDetails().getErrorCode();
                    }

                    FreeTextInformationType errorText = errorReturn.getErrorText();
                    if (errorText != null) {
                        errorTextList = errorText.getFreeText();
                    }
                }

                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setProvider("Amadeus");
                errorMessage.setType(ErrorMessage.ErrorType.ERROR);
                errorMessage.setGdsPNR(removePnrJson.getGdsPnr());

                if (errorCode != null) {
                    errorMessage.setErrorCode(errorCode);

                    switch (errorCode.toUpperCase()) {
                        case "79D":
                            errorMessage.setMessage("Queue identifier has not been assigned for specified office identification");
                            break;

                        case "91C":
                            errorMessage.setMessage("Invalid PNR");
                            break;

                    }
                }
                if (errorTextList != null && !errorTextList.isEmpty()) {
                    errorMessage.setMessage(errorTextList.get(0));
                }
                removePnrFromQueueRes.setMessage(errorMessage);
                
            }


        } catch (Exception e) {
            logger.error("Error getting queue list: {}", e.getMessage(), e);
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setProvider("Amadeus");
            errorMessage.setType(ErrorMessage.ErrorType.ERROR);
            errorMessage.setGdsPNR(removePnrJson.getGdsPnr());
            errorMessage.setErrorCode("");
            errorMessage.setMessage("Unexpected error occurred");
            removePnrFromQueueRes.setMessage(errorMessage);
            removePnrFromQueueRes.setSuccess(false);
        }

        return removePnrFromQueueRes;
    }

    @Override
    public PNRReply outwardMessageRequest (List<OutwardMessageDTO> outwardMessageList,String gdsPnr) {

        try {
            AmadeusSessionWrapper amadeusSessionWrapper = serviceHandler.logIn(true);
            serviceHandler.retrievePNR(gdsPnr, amadeusSessionWrapper);
            PNRReply pnrReply = serviceHandler.addOutwardMessagesToPNR(outwardMessageList, amadeusSessionWrapper);
            serviceHandler.savePNR(amadeusSessionWrapper);
            return pnrReply;
        } catch (Exception e) {
            logger.debug("Error while adding outward messages to Gds Pnr {} ", e.getMessage(), e);
            return null;
        }

    }

    @Override
    public List<OutwardMessageDTO> markOutwardMessagesStatus(List<OutwardMessageDTO> outwardMessageList, PNRReply pnrReply) {
        try {
            if (outwardMessageList == null || outwardMessageList.isEmpty() || pnrReply == null) {
                return Collections.emptyList();
            }

            List<OutwardMessageDTO> resultList = new ArrayList<>();
            List<PNRReply.DataElementsMaster.DataElementsIndiv> indivList =
                    pnrReply.getDataElementsMaster() != null ? pnrReply.getDataElementsMaster().getDataElementsIndiv() : Collections.emptyList();

            for (OutwardMessageDTO dto : outwardMessageList) {
                OutwardMessageDTO messageDTO = new OutwardMessageDTO();
                messageDTO.setPaxRef(dto.getPaxRef());
                messageDTO.setPaxName(dto.getPaxName());
                messageDTO.setSegment(dto.getSegment());
                messageDTO.setSegmentRef(dto.getSegmentRef());
                messageDTO.setRemarkType(dto.getRemarkType());
                messageDTO.setCarrierCode(dto.getCarrierCode());
                messageDTO.setFreeText(dto.getFreeText());

                String dtoText = dto.getFreeText() != null ? String.join("\n", dto.getFreeText()).trim().toUpperCase() : "";
                boolean found = false;
                boolean isError = false;

                for (PNRReply.DataElementsMaster.DataElementsIndiv indiv : indivList) {
                    String segmentName = indiv.getElementManagementData() != null ? indiv.getElementManagementData().getSegmentName() : null;
                    if (segmentName == null) continue;

                    if ("SSR".equalsIgnoreCase(segmentName) && indiv.getServiceRequest() != null && indiv.getServiceRequest().getSsr() != null) {
                        SpecialRequirementsTypeDetailsTypeI ssr = indiv.getServiceRequest().getSsr();
                        if (ssr.getFreeText() != null) {
                            String indivText = String.join("\n", ssr.getFreeText()).trim().toUpperCase();
                            if (indivText.contains(dtoText)) {
                                found = true;
                                if (indiv.getElementManagementData() != null && "ERR".equalsIgnoreCase(indiv.getElementManagementData().getStatus())) {
                                    isError = true;
                                    break;
                                }
                            }
                        }
                    }

                    if ("OS".equalsIgnoreCase(segmentName) && indiv.getOtherDataFreetext() != null) {
                        for (LongFreeTextType ft : indiv.getOtherDataFreetext()) {
                            String indivText = ft.getLongFreetext() != null ? ft.getLongFreetext().trim().toUpperCase() : "";
                            if (indivText.contains(dtoText)) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (("RM".equalsIgnoreCase(segmentName) || "RC".equalsIgnoreCase(segmentName) || "RX".equalsIgnoreCase(segmentName))
                            && indiv.getMiscellaneousRemarks() != null
                            && indiv.getMiscellaneousRemarks().getRemarks() != null
                            && indiv.getMiscellaneousRemarks().getRemarks().getFreetext() != null) {

                        String indivText = String.join("\n", indiv.getMiscellaneousRemarks().getRemarks().getFreetext()).trim().toUpperCase();
                        if (indivText.contains(dtoText)) {
                            found = true;
                        }
                    }
                }

                if (isError) {
                    messageDTO.setStatus("ERROR");
                } else {
                    messageDTO.setStatus(found ? "SUCCESS" : "FAILED");
                }

                resultList.add(messageDTO);
            }

            return resultList;

        } catch (Exception e) {
            logger.error("Error marking outward messages status", e);
            return Collections.emptyList();
        }
    }



}
