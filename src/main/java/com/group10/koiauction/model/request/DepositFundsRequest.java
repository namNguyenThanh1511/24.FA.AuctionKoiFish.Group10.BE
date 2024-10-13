package com.group10.koiauction.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class DepositFundsRequest {
    private double amount;
}
