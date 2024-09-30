package com.group10.koiauction.model.request;

import com.group10.koiauction.entity.enums.AccountRoleEnum;
import com.group10.koiauction.entity.enums.AccountStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})\\b", message = "Phone number is invalid")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

}
