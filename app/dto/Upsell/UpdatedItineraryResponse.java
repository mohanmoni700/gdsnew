package dto.Upsell;

import com.compassites.model.ErrorMessage;
import com.compassites.model.FlightItinerary;
import com.compassites.model.PAXFareDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatedItineraryResponse {

        private FlightItinerary flightItinerary;
        private ErrorMessage errorMessage;
        private String status;
        private List<PAXFareDetails> paxFareDetailsList;
        private BigDecimal pricePerAdult;
        private BigDecimal pricePerChild;
        private BigDecimal pricePerInfant;
        private BigDecimal totalPrice;
        private List<UpsellSegmentDetail> upsellSegmentDetailList;


        public UpdatedItineraryResponse(FlightItinerary flightItinerary, ErrorMessage errorMessage, String status, List<PAXFareDetails> paxFareDetailsList, BigDecimal pricePerAdult, BigDecimal pricePerChild, BigDecimal pricePerInfant, BigDecimal totalPrice, List<UpsellSegmentDetail> upsellSegmentDetailList) {
            this.flightItinerary = flightItinerary;
            this.errorMessage = errorMessage;
            this.status = status;
            this.paxFareDetailsList = paxFareDetailsList;
            this.pricePerAdult = pricePerAdult;
            this.pricePerChild = pricePerChild;
            this.pricePerInfant = pricePerInfant;
            this.totalPrice = totalPrice;
            this.upsellSegmentDetailList = upsellSegmentDetailList;
        }

        // Getters and Setters
        public FlightItinerary getFlightItinerary() {
            return flightItinerary;
        }

        public void setFlightItinerary(FlightItinerary flightItinerary) {
            this.flightItinerary = flightItinerary;
        }

        public ErrorMessage getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(ErrorMessage errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean hasError() {
            return errorMessage != null;
        }

        public List<PAXFareDetails> getPaxFareDetailsList() {
            return paxFareDetailsList;
        }

        public void setPaxFareDetailsList(List<PAXFareDetails> paxFareDetailsList) {
            this.paxFareDetailsList = paxFareDetailsList;
        }

        public BigDecimal getPricePerAdult() {
            return pricePerAdult;
        }

        public void setPricePerAdult(BigDecimal pricePerAdult) {
            this.pricePerAdult = pricePerAdult;
        }

        public BigDecimal getPricePerChild() {
            return pricePerChild;
        }

        public void setPricePerChild(BigDecimal pricePerChild) {
            this.pricePerChild = pricePerChild;
        }

        public BigDecimal getPricePerInfant() {
            return pricePerInfant;
        }

        public void setPricePerInfant(BigDecimal pricePerInfant) {
            this.pricePerInfant = pricePerInfant;
        }

        public BigDecimal getTotalPrice() {
                return totalPrice;
            }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public List<UpsellSegmentDetail> getUpsellSegmentDetailList() {
            return upsellSegmentDetailList;
        }

        public void setUpsellSegmentDetailList(List<UpsellSegmentDetail> upsellSegmentDetailList) {
            this.upsellSegmentDetailList = upsellSegmentDetailList;
        }
}
