package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor // Add this annotation
public class AccountResponsePagination {
    private List<AccountResponse> accountResponseList;
    private int pageNumber;
    private int totalPages;
    private int totalElements;
}

