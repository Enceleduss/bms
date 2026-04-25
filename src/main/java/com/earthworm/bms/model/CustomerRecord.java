package com.earthworm.bms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@NoArgsConstructor
@Table(name = "users")
@Getter
@Setter
public class CustomerRecord extends GraphNode implements UserDetails {
    
    @Column
    private String name;
    @Column(length = 64)
    private String email;

    @Column
    private String username;
    @JsonIgnore
    @Column
    private String password;
    @Column(length = 1024)
    private String address;
    @Column(length = 50)
    private String pan;
    @Column(length = 64)
    private String uid;
    @Column(length = 64)
    private String acctype;
    @Column(length = 64)
    private String branchname;
    @Column(length = 64)
    private String country;
    @Column
    private long dob;
    @Column(length = 64)
    private String docnum;
    @Column(length = 64)
    private String identificationtype;
    @Column
    private double initialdeposit;
    @Column
    private double balance;
    @Column(length = 64)
    private String phone;
    @Column(length = 64)
    private String state;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    public CustomerRecord(long userId, String username, String password, Set<Role> authorities) {
        super();
        this.id = userId;
        this.name = username;
        this.username = username;
        this.password = password;
        this.roles = authorities;
    }

    public CustomerRecord(String name, String email, String userName, String password, String address, String pan, String uid, Set<Role> roles) {
        this.name = name;
        this.email = email;
        this.username = userName;
        this.password = password;
        this.address = address;
        this.pan = pan;
        this.uid = uid;
        this.roles = roles;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void depositBalance(double balance) {
        this.balance = this.balance + balance;
    }

    @Override
    public String toString() {
        return "CustomerRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", roles=" + roles +
                '}';
    }
}
