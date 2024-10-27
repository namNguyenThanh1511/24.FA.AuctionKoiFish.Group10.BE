package com.group10.koiauction.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithDrawRequestDTO {
    private String bankAccountNumber;
    private String bankName;
    private String bankAccountName;
    private double amount;
}
