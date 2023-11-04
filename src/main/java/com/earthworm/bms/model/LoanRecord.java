package com.earthworm.bms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EducationLoan.class, name = "edu"),
        @JsonSubTypes.Type(value = OtherLoanRecord.class, name = "oth")
})
public class LoanRecord {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected Integer id;
    @JsonProperty("userid")
    @Column
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    @JsonProperty("loantype")
    @Column
    private String loanType;
    @JsonProperty("loandate")
    @Column(length = 64)
    private long loanDate;
    @JsonProperty("loanamount")
    @Column(length = 64)
    private double loanAmount;
    @JsonProperty("loanduration")
    @Column
    private int loanDuration;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public long getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(long loandate) {
        this.loanDate = loandate;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public int getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(int loanDuration) {
        this.loanDuration = loanDuration;
    }
}
