package com.group10.koiauction.model.response;
import lombok.Data;
import java.util.List;
@Data
public class TransactionResponsePaginationDTO {
    private List<TransactionResponseDTO> transactionResponseList; // Assuming AccountResponseDTO is already defined
    private int pageNumber;
    private int totalPages;
    private long totalElements;
}