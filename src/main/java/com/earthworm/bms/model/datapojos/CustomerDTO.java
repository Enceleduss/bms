package com.earthworm.bms.model.datapojos;

// A simple DTO (Data Transfer Object) to hold customer data for the view layer.
// Using a DTO is a best practice to decouple the view from the database entity.
public class CustomerDTO {
    public final String name;
    public final String email;
    public final String username;
    public final String acctype;
    public final double balance;

    public CustomerDTO(String name, String email, String username, String acctype, double balance) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.acctype = acctype;
        this.balance = balance;
    }
}
