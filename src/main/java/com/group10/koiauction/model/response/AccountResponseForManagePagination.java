package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseForManagePagination {
    private List<AccountResponseForManageDTO> accountResponseList;
    private int pageNumber;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;
}
