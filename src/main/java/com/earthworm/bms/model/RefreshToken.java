package com.earthworm.bms.model;

import jakarta.persistence.*;


import java.time.Instant;


@Entity
public class RefreshToken {
    public RefreshToken() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;



    @Column
    private String userid;

    @Column(nullable = false, unique = true, length = 4096)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
//getters and setters

    public RefreshToken(String user, String token, Instant expiryDate) {
        this.userid = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
