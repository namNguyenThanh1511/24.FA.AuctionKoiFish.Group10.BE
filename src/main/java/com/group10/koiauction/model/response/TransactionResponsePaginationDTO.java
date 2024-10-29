package com.group10.koiauction.model.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponsePaginationDTO {
    private List<TransactionResponseDTO> transactionResponseList;
    private int pageNumber;
    private int totalPages;
    private long totalElements;
}