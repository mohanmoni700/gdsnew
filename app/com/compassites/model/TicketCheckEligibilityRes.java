package com.compassites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dto.refund.PerPaxRefundPricingInformation;
import org.intellij.lang.annotations.JdkConstants;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketCheckEligibilityRes {

    private Boolean status;
    private BigDecimal refundableAmount;
    private String currency;
    private String formOfPayment;
    private ErrorMessage message;
    private String provider;
    List<PerPaxRefundPricingInformation> perPaxRefundPricingInformationList;

    public ErrorMessage getMessage() {
        return message;
    }

    public void setMessage(ErrorMessage message) {
        this.message = message;
    }

    public String getFormOfPayment() {
        return formOfPayment;
    }

    public void setFormOfPayment(String formOfPayment) {
        this.formOfPayment = formOfPayment;
    }
    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public BigDecimal getRefundableAmount() {
        return refundableAmount;
    }

    public void setRefundableAmount(BigDecimal refundableAmount) {
        this.refundableAmount = refundableAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<PerPaxRefundPricingInformation> getPerPaxRefundPricingInformationList() {
        return perPaxRefundPricingInformationList;
    }

    public void setPerPaxRefundPricingInformationList(List<PerPaxRefundPricingInformation> perPaxRefundPricingInformationList) {
        this.perPaxRefundPricingInformationList = perPaxRefundPricingInformationList;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

}
