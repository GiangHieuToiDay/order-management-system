package com.trainning.ordersystem.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    private boolean isFirst;

    private boolean isLast;

    @JsonProperty("first")
    public boolean getFirst() {
        return isFirst;
    }

    @JsonProperty("last")
    public boolean getLast() {
        return isLast;
    }

    @JsonProperty("isFirst")
    public boolean getIsFirst() {
        return isFirst;
    }

    @JsonProperty("isLast")
    public boolean getIsLast() {
        return isLast;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
