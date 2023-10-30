package com.earthworm.bms.model.datapojos;

import com.earthworm.bms.model.CustomerRecord;

public class LoginResponseDTO {
    private CustomerRecord user;
    private String jwt;
    //private String refreshToken;

    public LoginResponseDTO(){
        super();
    }

    public LoginResponseDTO(CustomerRecord user, String jwt){
        this.user = user;
        this.jwt = jwt;
       // this.refreshToken = refreshToken;
    }

    public CustomerRecord getUser(){
        return this.user;
    }

    public void setUser(CustomerRecord user){
        this.user = user;
    }

    public String getJwt(){
        return this.jwt;
    }



    public void setJwt(String jwt){
        this.jwt = jwt;
    }

}
