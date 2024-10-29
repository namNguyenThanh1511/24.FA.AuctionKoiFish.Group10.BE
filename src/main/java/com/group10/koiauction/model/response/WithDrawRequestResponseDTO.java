package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.WithDrawRequestEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithDrawRequestResponseDTO {
    private Long id;
    private String bankAccountNumber;
    private String bankName;
    private String bankAccountName;
    private double amount;
    private String responseNote;
    private String image_url;
    private LocalDateTime createdAt;
    private WithDrawRequestEnum status;
    private AccountResponseSimplifiedDTO user;
    private AccountResponseSimplifiedDTO staff;
}
