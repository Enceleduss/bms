package com.earthworm.bms.model.datapojos;

import com.earthworm.bms.model.CustomerRecord;

public class DepositDTO {
    private String acctype;
    double deposit;
    //private String refreshToken;

    public DepositDTO(){
        super();
    }

    public DepositDTO(String acctype, double deposit) {
        this.acctype = acctype;
        this.deposit = deposit;
    }

    public String getAcctype() {
        return acctype;
    }

    public void setAcctype(String acctype) {
        this.acctype = acctype;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }
}
