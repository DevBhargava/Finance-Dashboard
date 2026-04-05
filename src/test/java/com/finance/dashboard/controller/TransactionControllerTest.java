package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.enums.TransactionCategory;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TransactionService transactionService;

    // ---------- GET /api/transactions ----------

    @Test
    @DisplayName("VIEWER can list transactions")
    @WithMockUser(roles = "VIEWER")
    void getTransactions_asViewer_returns200() throws Exception {
        PagedResponse<TransactionResponse> empty = PagedResponse.<TransactionResponse>builder()
                .content(List.of()).page(0).size(10).totalElements(0).totalPages(0).last(true).build();

        when(transactionService.getTransactions(any())).thenReturn(empty);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthenticated request is rejected")
    void getTransactions_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- POST /api/transactions ----------

    @Test
    @DisplayName("ANALYST can create a transaction")
    @WithMockUser(roles = "ANALYST")
    void createTransaction_asAnalyst_returns201() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory(TransactionCategory.UTILITIES);
        request.setDate(LocalDate.now());
        request.setDescription("Electricity bill");

        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .build();

        when(transactionService.createTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(500.00))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("VIEWER cannot create a transaction — returns 403")
    @WithMockUser(roles = "VIEWER")
    void createTransaction_asViewer_returns403() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory(TransactionCategory.FREELANCE);
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validation fails when amount is missing")
    @WithMockUser(roles = "ANALYST")
    void createTransaction_missingAmount_returns400() throws Exception {
        // missing amount on purpose
        String body = """
                {
                    "type": "EXPENSE",
                    "category": "FOOD",
                    "date": "2024-03-01"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.amount").exists());
    }

    // ---------- DELETE /api/transactions/{id} ----------

    @Test
    @DisplayName("ANALYST cannot delete — returns 403")
    @WithMockUser(roles = "ANALYST")
    void deleteTransaction_asAnalyst_returns403() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can delete a transaction")
    @WithMockUser(roles = "ADMIN")
    void deleteTransaction_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
