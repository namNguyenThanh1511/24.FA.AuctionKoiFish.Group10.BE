package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuctionSessionResponsePagination {
    private List<AuctionSessionResponsePrimaryDataDTO> auctionSessionResponses;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}