package com.group10.koiauction.model.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequestDTO {
    @Size(min = 6 , message = "Password must be exceed 6 characters ")
    String password;
}
