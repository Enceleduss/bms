package com.earthworm.bms.model.datapojos;

import com.earthworm.bms.model.Role;
import jakarta.persistence.*;

import java.util.Set;

public class RegistrationDetailsDTO {
    private String name;
    private String email;
    private String userName;
    private String password;
    private String address;
    private String pan;
    private String uid;



    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getPan() {
        return pan;
    }

    public String getUid() {
        return uid;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    private Set<Role> roles;



    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    @Override
    public String toString()
    {
        return "name "+this.name+" user name "+this.getUserName()+" email "+this.getEmail()+" roles "+this.getRoles().stream().findFirst().get();
    }
}
