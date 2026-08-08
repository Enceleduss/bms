package com.earthworm.bms.model.datapojos;

import com.earthworm.bms.model.GraphNode;

/**
 * A DTO for the view layer. 
 * Inherits from GraphNode to enable automatic dependency tracking for reactive updates.
 */
public class CustomerDTO extends GraphNode {
    public final String name;
    public final String email;
    public final String username;
    public final String acctype;
    public final double balance;

    public CustomerDTO(long id, String name, String email, String username, String acctype, double balance) {
        this.setId(id);
        this.name = name;
        this.email = email;
        this.username = username;
        this.acctype = acctype;
        this.balance = balance;
    }
}
