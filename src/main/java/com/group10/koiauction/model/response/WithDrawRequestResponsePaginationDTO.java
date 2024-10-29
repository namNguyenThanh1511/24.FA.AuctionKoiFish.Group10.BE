package com.group10.koiauction.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithDrawRequestResponsePaginationDTO {
    private List<WithDrawRequestResponseDTO> withDrawRequestResponseDTOList;
    private int pageNumber;
    private int totalPages;
    private long totalElements;
}
