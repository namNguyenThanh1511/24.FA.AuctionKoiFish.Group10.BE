package com.group10.koiauction.model.response;

import com.group10.koiauction.entity.enums.KoiSexEnum;
import com.group10.koiauction.entity.enums.KoiStatusEnum;
import lombok.Data;


import java.util.List;


@Data
public class KoiFishResponsePagination {
    private List<KoiFishResponse> koiFishResponseList;
    private int pageNumber;
    private long totalElements;
    private int numberOfElements;
    private int totalPages;

}
