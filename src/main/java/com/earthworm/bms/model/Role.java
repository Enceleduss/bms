package com.earthworm.bms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Setter
@Getter
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 60)
    private String name;

    @Column(length = 60)
    private String authority;

    public Role(String name) {
        super();
        this.name = name;
        this.authority = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

    @Override
    public String toString() {
        return this.getAuthority()+" name "+this.getName();
    }
}
