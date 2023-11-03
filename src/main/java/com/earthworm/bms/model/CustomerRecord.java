package com.earthworm.bms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
public class CustomerRecord implements UserDetails {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;
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
    public CustomerRecord(Integer userId, String username, String password, Set<Role> authorities) {
        super();
        this.id = userId;
        this.name = username;
        this.username = username;
        this.password = password;
        this.roles = authorities;
    }
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.username;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAcctype() {
        return acctype;
    }

    public void setAcctype(String acctype) {
        this.acctype = acctype;
    }

    public String getBranchname() {
        return branchname;
    }

    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getDob() {
        return dob;
    }

    public void setDob(long dob) {
        this.dob = dob;
    }

    public String getDocnum() {
        return docnum;
    }

    public void setDocnum(String docnum) {
        this.docnum = docnum;
    }

    public String getIdentificationtype() {
        return identificationtype;
    }

    public void setIdentificationtype(String identificationtype) {
        this.identificationtype = identificationtype;
    }

    public double getInitialdeposit() {
        return initialdeposit;
    }

    public void setInitialdeposit(double initialdeposit) {
        this.initialdeposit = initialdeposit;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void depositBalance(double balance) {
        this.balance = this.balance + balance;
    }
}
