package com.earthworm.bms.model.datapojos;

import com.earthworm.bms.model.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.persistence.*;

import java.util.Set;

import static java.util.Arrays.stream;

public class RegistrationDetailsDTO {
    private String name;
    private String email;
    private String username;
    private String password;
    private String address;
    private String pan;
    private String uid;

    private String acctype;
    private String branchname;
    private String country;
    private long dob;
    private String docnum;
    private String identificationtype;
    private double initialdeposit;
    private String phone;
    private String state;

    public String getAcctype() {
        return acctype;
    }

    public void setAcctype(String acctype) {
        this.acctype = acctype;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getDob() {
        return dob;
    }

    public void setDob(long dob) {
        this.dob = dob;
    }

    public String getDocnum() {
        return docnum;
    }

    public void setDocnum(String docnum) {
        this.docnum = docnum;
    }

    public String getIdentificationtype() {
        return identificationtype;
    }

    public void setIdentificationtype(String identificationtype) {
        this.identificationtype = identificationtype;
    }

    public double getInitialdeposit() {
        return initialdeposit;
    }

    public void setInitialdeposit(double initialdeposit) {
        this.initialdeposit = initialdeposit;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
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

    public void setUsername(String userName) {
        this.username = userName;
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
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = ow.writeValueAsString(this);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //return "name "+this.name+" user name "+this.getUserName()+" email "+this.getEmail()+" roles "+this.getRoles()?.stream().findFirst().get();
    }
}
