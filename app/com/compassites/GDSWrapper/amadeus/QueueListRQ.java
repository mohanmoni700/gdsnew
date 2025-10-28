package com.compassites.GDSWrapper.amadeus;

import com.amadeus.xml.qdqlrq_11_1_1a.*;
import com.amadeus.xml.quqmdq_03_1_1a.AdditionalBusinessSourceInformationType;
import com.amadeus.xml.quqmdq_03_1_1a.QueueRemoveItem;
import com.amadeus.xml.quqmdq_03_1_1a.ReservationControlInformationDetailsTypeI;
import com.amadeus.xml.quqmdq_03_1_1a.ReservationControlInformationTypeI;
import dto.queueManagement.QueueListIndividualReqDTO;
import dto.queueManagement.RemovePnrFomQueueDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class QueueListRQ {

    private static final Logger logger = LoggerFactory.getLogger("gds");

    public static QueueList queueListRequest(QueueListIndividualReqDTO queueListRQ) {

        QueueList queueList = new QueueList();

        try {

            AdditionalBusinessSourceInformationTypeI targetOffice = new AdditionalBusinessSourceInformationTypeI();

            SourceTypeDetailsTypeI sourceType = new SourceTypeDetailsTypeI();
            sourceType.setSourceQualifier1("3");
            targetOffice.setSourceType(sourceType);

            OriginatorIdentificationDetailsTypeI originatorDetails = new OriginatorIdentificationDetailsTypeI();
            originatorDetails.setInHouseIdentification1(queueListRQ.getOfficeId());
            targetOffice.setOriginatorDetails(originatorDetails);

            queueList.setTargetOffice(targetOffice);

            // Set queue number
            QueueInformationTypeI queueNumber = new QueueInformationTypeI();
            QueueInformationDetailsTypeI queueDetails = new QueueInformationDetailsTypeI();
            queueDetails.setNumber(queueListRQ.getQueueNumber());
            queueNumber.setQueueDetails(queueDetails);
            queueList.setQueueNumber(queueNumber);

            // Set category number
            SubQueueInformationTypeI categoryDetails = new SubQueueInformationTypeI();
            SubQueueInformationDetailsTypeI subQueueInfoDetails = new SubQueueInformationDetailsTypeI();
            subQueueInfoDetails.setIdentificationType("C");
            subQueueInfoDetails.setItemNumber(queueListRQ.getCategoryNumber().toString());
            categoryDetails.setSubQueueInfoDetails(subQueueInfoDetails);
            queueList.setCategoryDetails(categoryDetails);

            // Setting date range for dual type queues
            if (queueListRQ.getDateRange() != null) {
                StructuredDateTimeInformationType date = new StructuredDateTimeInformationType();
                date.setTimeMode(queueListRQ.getDateRange());
                queueList.setDate(date);
            }

//            UserIdentificationType agentSine = new UserIdentificationType();
//            agentSine.setOriginator("WS");
//            queueList.setAgentSine(agentSine);

            return queueList;
        } catch (Exception e) {
            logger.error("Error in queue list api", e);
            return null;
        }


    }

    public static QueueRemoveItem removeItemFromQueue(RemovePnrFomQueueDTO removePnrDto) {

        QueueRemoveItem queueRemoveItem = new QueueRemoveItem();

        try {

            com.amadeus.xml.quqmdq_03_1_1a.SelectionDetailsTypeI removalOption = new com.amadeus.xml.quqmdq_03_1_1a.SelectionDetailsTypeI();
            com.amadeus.xml.quqmdq_03_1_1a.SelectionDetailsInformationTypeI selectionDetails = new com.amadeus.xml.quqmdq_03_1_1a.SelectionDetailsInformationTypeI();

            selectionDetails.setOption("QRP");
            removalOption.setSelectionDetails(selectionDetails);

            QueueRemoveItem.TargetDetails targetDetails1 = new QueueRemoveItem.TargetDetails();

            AdditionalBusinessSourceInformationType targetOffice = new AdditionalBusinessSourceInformationType();

            com.amadeus.xml.quqmdq_03_1_1a.SourceTypeDetailsTypeI sourceType = new com.amadeus.xml.quqmdq_03_1_1a.SourceTypeDetailsTypeI();
            sourceType.setSourceQualifier1("3");
            com.amadeus.xml.quqmdq_03_1_1a.OriginatorIdentificationDetailsTypeI originatorDetails = new com.amadeus.xml.quqmdq_03_1_1a.OriginatorIdentificationDetailsTypeI();
            originatorDetails.setInHouseIdentification1(removePnrDto.getIata());
            targetOffice.setSourceType(sourceType);
            targetOffice.setOriginatorDetails(originatorDetails);

            com.amadeus.xml.quqmdq_03_1_1a.QueueInformationTypeI queueNumber = new com.amadeus.xml.quqmdq_03_1_1a.QueueInformationTypeI();
            com.amadeus.xml.quqmdq_03_1_1a.QueueInformationDetailsTypeI queueDetails = new com.amadeus.xml.quqmdq_03_1_1a.QueueInformationDetailsTypeI();
            queueDetails.setNumber(BigInteger.valueOf(Long.parseLong(removePnrDto.getQueueNumber())));
            queueNumber.setQueueDetails(queueDetails);

            com.amadeus.xml.quqmdq_03_1_1a.SubQueueInformationTypeI categoryDetails = new com.amadeus.xml.quqmdq_03_1_1a.SubQueueInformationTypeI();
            com.amadeus.xml.quqmdq_03_1_1a.SubQueueInformationDetailsTypeI subQueueInfoDetails = new com.amadeus.xml.quqmdq_03_1_1a.SubQueueInformationDetailsTypeI();
            subQueueInfoDetails.setIdentificationType("C");
            subQueueInfoDetails.setItemNumber(removePnrDto.getCategoryNumber());
            categoryDetails.setSubQueueInfoDetails(subQueueInfoDetails);

            if (removePnrDto.getDateRange() != null) {
                com.amadeus.xml.quqmdq_03_1_1a.StructuredDateTimeInformationType placementDate = new com.amadeus.xml.quqmdq_03_1_1a.StructuredDateTimeInformationType();
                placementDate.setTimeMode(BigInteger.valueOf(Long.parseLong(removePnrDto.getDateRange())));
                targetDetails1.setPlacementDate(placementDate);
            }

            ReservationControlInformationTypeI reservationControlInformationTypeI = new ReservationControlInformationTypeI();
            ReservationControlInformationDetailsTypeI reservation = new ReservationControlInformationDetailsTypeI();
            reservation.setControlNumber(removePnrDto.getGdsPnr());
            reservationControlInformationTypeI.setReservation(reservation);

            targetDetails1.setTargetOffice(targetOffice);
            targetDetails1.setQueueNumber(queueNumber);
            targetDetails1.setCategoryDetails(categoryDetails);
            targetDetails1.getRecordLocator().add(reservationControlInformationTypeI);

            queueRemoveItem.setRemovalOption(removalOption);
            queueRemoveItem.getTargetDetails().add(targetDetails1);

            return queueRemoveItem;
        } catch (Exception e) {
            logger.error("Error in queue remove item api", e);
            return null;
        }
    }

}
