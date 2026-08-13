package com.earthworm.bms.model;

import jakarta.persistence.*;

/**
 * This entity is specifically designed to generate and reserve IDs from the "NODE_ID" sequence.
 * It provides a clean, JPA-compliant way to get the next value from the sequence.
 */
@Entity
@Table(name = "node_id_sequence")
public class NodeIDSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "node_id_generator")
    @SequenceGenerator(
        name = "node_id_generator",
        sequenceName = "node_id",
        allocationSize = 1
    )
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}