package com.group10.koiauction.model.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "Invalid email")
    @Column(unique = true)
    String email;
}
