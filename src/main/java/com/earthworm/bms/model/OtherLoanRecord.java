package com.earthworm.bms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class OtherLoanRecord extends LoanRecord{
    @JsonProperty("companyname")
    @Column
    private String companyName;
    @JsonProperty("companyaddress")
    @Column
    private String companyAddress;
    @JsonProperty("designation")
    @Column
    private String designation;
    @JsonProperty("annualincome")
    @Column
    private double annualIncome;
    @JsonProperty("totalexp")
    @Column
    private String totalExperience;
    @JsonProperty("currexp")
    @Column
    private String currentExperience;
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public double getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(double annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(String totalExperience) {
        this.totalExperience = totalExperience;
    }

    public String getCurrentExperience() {
        return currentExperience;
    }

    public void setCurrentExperience(String currentExperience) {
        this.currentExperience = currentExperience;
    }
}
