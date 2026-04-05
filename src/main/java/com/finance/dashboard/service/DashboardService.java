package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardSummaryResponse getSummary(int year) {
        BigDecimal totalIncome   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> categoryBreakdown = buildCategoryBreakdown();
        List<TransactionResponse> recentActivity  = buildRecentActivity();
        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends(year);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalTransactions(transactionRepository.countByDeletedFalse())
                .categoryBreakdown(categoryBreakdown)
                .recentActivity(recentActivity)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    public List<TransactionResponse> getWeeklyActivity() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        return transactionRepository.findByDateRange(weekAgo, today)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    // ---------- private helpers ----------

    private Map<String, BigDecimal> buildCategoryBreakdown() {
        List<Object[]> rows = transactionRepository.sumByCategory();
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] row : rows) {
            // row[0] = TransactionCategory enum, row[1] = sum
            breakdown.put(row[0].toString(), (BigDecimal) row[1]);
        }
        return breakdown;
    }

    private List<TransactionResponse> buildRecentActivity() {
        return transactionRepository
                .findRecentTransactions(PageRequest.of(0, 8))
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    private List<DashboardSummaryResponse.MonthlyTrend> buildMonthlyTrends(int year) {
        List<Object[]> rows = transactionRepository.monthlyBreakdown(year);

        // Build a map: monthNumber -> {INCOME: x, EXPENSE: y}
        Map<Integer, Map<String, BigDecimal>> monthMap = new TreeMap<>();
        for (Object[] row : rows) {
            int monthNum = ((Number) row[0]).intValue();
            String type  = row[1].toString();
            BigDecimal amount = (BigDecimal) row[2];

            monthMap.computeIfAbsent(monthNum, k -> new HashMap<>()).put(type, amount);
        }

        List<DashboardSummaryResponse.MonthlyTrend> trends = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, BigDecimal>> entry : monthMap.entrySet()) {
            String monthLabel = Month.of(entry.getKey())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Map<String, BigDecimal> totals = entry.getValue();

            trends.add(DashboardSummaryResponse.MonthlyTrend.builder()
                    .month(monthLabel)
                    .income(totals.getOrDefault("INCOME", BigDecimal.ZERO))
                    .expense(totals.getOrDefault("EXPENSE", BigDecimal.ZERO))
                    .build());
        }
        return trends;
    }
}
