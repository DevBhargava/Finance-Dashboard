package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Summary and analytics endpoints — accessible by all authenticated users")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(
        summary = "Full dashboard summary",
        description = "Returns total income, total expenses, net balance, category breakdown, " +
                      "recent transactions, and monthly trends for the given year."
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(defaultValue = "0") int year) {

        // default to current year when caller doesn't specify
        int targetYear = (year == 0) ? LocalDate.now().getYear() : year;
        DashboardSummaryResponse summary = dashboardService.getSummary(targetYear);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/weekly")
    @Operation(
        summary = "Last 7-day activity",
        description = "Returns all transactions from the past 7 days, useful for a quick overview widget."
    )
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getWeeklyActivity() {
        List<TransactionResponse> activity = dashboardService.getWeeklyActivity();
        return ResponseEntity.ok(ApiResponse.ok(activity));
    }
}
