package com.earthworm.bms.model;

import com.earthworm.bms.service.reactive.DependencyTracker;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
//@JsonSubTypes({
//      @JsonSubTypes.Type(value = EducationLoan.class, name = "edu"),
//        @JsonSubTypes.Type(value = OtherLoanRecord.class, name = "oth")
//})
public class GraphNode {
    @Id
    @Column(nullable = false)
    protected long id;
    @Column(nullable = false)
    protected long parentid;
    @Column(nullable = false)
    protected String type;

    public long getId() {
        // Whenever the ID is accessed (which happens during any property access in GraalVM),
        // we record it as a dependency.
        DependencyTracker.recordAccess(id);
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getParentid() {
        return parentid;
    }

    public void setParentid(long parentid) {
        this.parentid = parentid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
