package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.enums.TransactionCategory;
import com.finance.dashboard.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    Page<Transaction> findAllByDeletedFalse(Pageable pageable);

    // total income / expense
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deleted = false")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // sum by category
    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deleted = false GROUP BY t.category")
    List<Object[]> sumByCategory();

    // monthly breakdown for a given year
    @Query("SELECT MONTH(t.date), t.type, COALESCE(SUM(t.amount), 0) " +
           "FROM Transaction t WHERE YEAR(t.date) = :year AND t.deleted = false " +
           "GROUP BY MONTH(t.date), t.type ORDER BY MONTH(t.date)")
    List<Object[]> monthlyBreakdown(@Param("year") int year);

    // recent activity (not deleted)
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(Pageable pageable);

    // weekly summary for the last N days
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false AND t.date >= :from AND t.date <= :to ORDER BY t.date DESC")
    List<Transaction> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByDeletedFalse();
}
