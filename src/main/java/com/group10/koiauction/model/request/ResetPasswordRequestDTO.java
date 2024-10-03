package com.group10.koiauction.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequestDTO {
    @Size(min = 6 , message = "Password must be exceed 6 characters ")
    String password;
}
