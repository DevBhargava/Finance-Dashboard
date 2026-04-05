package com.finance.dashboard.dto.request;

import com.finance.dashboard.enums.TransactionCategory;
import com.finance.dashboard.enums.TransactionType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * All fields are optional — only those present will be applied as filters.
 */
@Data
public class TransactionFilterRequest {

    private TransactionType type;

    private TransactionCategory category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    // free-text search on description
    private String keyword;

    // pagination
    private int page = 0;
    private int size = 10;

    // sorting: field name, e.g. "date", "amount"
    private String sortBy = "date";
    private String sortDir = "desc";
}
