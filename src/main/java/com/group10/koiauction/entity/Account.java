package com.group10.koiauction.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long user_id;



    @Column(unique = true)
    private String username;



    private String password;

    private String firstName;


    private String lastName;


    @Column(unique = true)
    private String email;


    @Column(unique = true)
    private String phoneNumber;

    private String address;

    private Date createdDate = new Date();


    private Date updatedDate = new Date();

    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status = AccountStatusEnum.ACTIVE;
    @Enumerated(EnumType.STRING)
    private AccountRoleEnum roleEnum;

    @NotNull(message = "Balance must not be null")
    @Column(name = "balance",nullable = false)
    private double balance;

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    Set<KoiFish> koiFishSet;

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    Set<AuctionRequest> auctionRequestSet;


    @OneToMany(mappedBy = "winner")//1 member có thể thắng nhiều phiên đấu giá
    @JsonIgnore
    Set<AuctionSession> auctionSessionSet;

    @OneToMany(mappedBy = "staff")
    @JsonIgnore
    Set<AuctionSession> auctionSessionsSetForStaff;

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    Set<Bid> bidSet;

    @OneToMany(mappedBy = "from")
    Set<Transaction> transactionSetFrom;

    @OneToMany(mappedBy = "to")
    @JsonIgnore
    Set<Transaction> transactionSetTo;

    @OneToMany(mappedBy = "member")
    Set<PaymentRequest> paymentRequestSet;




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // đinh nghĩa quyền hạn account này làm đc
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(this.roleEnum != null) {
            authorities.add(new SimpleGrantedAuthority(this.roleEnum.toString()));
        }
        return authorities;
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
}