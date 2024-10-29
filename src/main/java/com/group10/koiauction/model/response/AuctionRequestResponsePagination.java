package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuctionRequestResponsePagination {
    private List<AuctionRequestResponse> auctionRequestResponseList;
    private int pageNumber;
    private int totalPages;
    private int numberOfElements;
    private long totalElements;

}
