package com.earthworm.bms.model;

import com.earthworm.bms.service.reactive.DependencyTracker;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GraphNode {
    @Id
    @SequenceGenerator(
            name = "graph_node_id_seq",      // A unique name for this generator in your application
            sequenceName = "graph_node_id_seq", // The actual name of the sequence in your PostgreSQL database
            allocationSize = 1              // How many IDs to pre-fetch (1 is safest for most apps)
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "graph_node_id_seq"   // Reference the generator defined above
    )
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
