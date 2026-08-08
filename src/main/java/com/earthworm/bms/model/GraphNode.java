package com.earthworm.bms.model;

import com.earthworm.bms.service.reactive.DependencyTracker;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GraphNode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;

    @Column(nullable = true)
    protected Long parentid;

    @Column(nullable = true)
    protected String type;

    public long getId() {
        // Whenever the ID is accessed, we record it as a dependency.
        DependencyTracker.recordAccess(id);
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getParentid() {
        return parentid;
    }

    public void setParentid(Long parentid) {
        this.parentid = parentid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
