package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.enums.TransactionCategory;
import com.finance.dashboard.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasCategory(TransactionCategory category) {
        if (category == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Transaction> dateAfter(LocalDate from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Transaction> dateBefore(LocalDate to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to);
    }

    public static Specification<Transaction> descriptionLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Transaction> buildFilter(
            TransactionType type,
            TransactionCategory category,
            LocalDate from,
            LocalDate to,
            String keyword) {

        return Specification.where(notDeleted())
                .and(hasType(type))
                .and(hasCategory(category))
                .and(dateAfter(from))
                .and(dateBefore(to))
                .and(descriptionLike(keyword));
    }
}
