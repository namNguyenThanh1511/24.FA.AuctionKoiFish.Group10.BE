package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.Account;
import lombok.Data;

@Data
public class EmailDetail {
    Account account;
    String subject;
    String link;
}
