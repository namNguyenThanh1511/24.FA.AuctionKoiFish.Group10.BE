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

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b", message = "Phone number is invalid")
    @Column(unique = true)
    private String phoneNumber;


    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;


    @NotNull(message = "Account creation date is required")
    @Past(message = "Creation date time must after current time")
    private Date createdDate = new Date();

    @NotNull(message = "Account update date is required")
    @Past(message = "Update date time must after current time")
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