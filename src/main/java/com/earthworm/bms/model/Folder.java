package com.earthworm.bms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Folder extends GraphNode {
    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    public Folder(GraphNode node) {
        this.setId(node.getId());
        this.setParentid(node.getParentid());
        this.setType(node.getType());
    }

    public Folder(GraphNode node, String name, String description) {
        this(node);
        this.name = name;
        this.description = description;
    }

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

    public Folder setGraph(GraphNode gNode) {
        this.setId(gNode.getId());
        this.setParentid(gNode.getParentid());
        this.setType(gNode.getType());
        return this;
    }
}