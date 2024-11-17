package com.earthworm.bms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Folder extends GraphNode{
    @Column
    String name;
    @Column
    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
