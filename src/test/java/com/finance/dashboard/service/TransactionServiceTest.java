package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionFilterRequest;
import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.TransactionCategory;
import com.finance.dashboard.enums.TransactionType;
import com.finance.dashboard.enums.UserStatus;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test Analyst")
                .email("analyst@test.com")
                .password("encoded")
                .role(Role.ANALYST)
                .status(UserStatus.ACTIVE)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("1500.00"))
                .type(TransactionType.INCOME)
                .category(TransactionCategory.SALARY)
                .date(LocalDate.now())
                .description("Monthly salary")
                .createdBy(testUser)
                .build();

        // wire up security context mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getEmail());
    }

    @SuppressWarnings("unchecked")
@Test
    @DisplayName("Should return paged transactions for a valid filter")
    void getTransactions_validFilter_returnsPagedResult() {
        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setPage(0);
        filter.setSize(10);

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        PagedResponse<TransactionResponse> result = transactionService.getTransactions(filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.getContent().get(0).getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    @DisplayName("Should throw BadRequestException when from date is after to date")
    void getTransactions_invalidDateRange_throwsBadRequest() {
        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setFrom(LocalDate.now());
        filter.setTo(LocalDate.now().minusDays(5));

        assertThatThrownBy(() -> transactionService.getTransactions(filter))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("'from' date cannot be after 'to' date");
    }

    @Test
    @DisplayName("Should return transaction by ID when it exists")
    void getTransactionById_exists_returnsResponse() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(testTransaction));

        TransactionResponse response = transactionService.getTransactionById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategory()).isEqualTo(TransactionCategory.SALARY);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction does not exist")
    void getTransactionById_notFound_throwsException() {
        when(transactionRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create transaction and return saved response")
    void createTransaction_validRequest_savesAndReturns() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("250.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategory(TransactionCategory.FOOD);
        request.setDate(LocalDate.now());
        request.setDescription("Groceries");

        when(userRepository.findByEmailAndDeletedFalse(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        Transaction saved = Transaction.builder()
                .id(2L)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(testUser)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should soft-delete a transaction by setting deleted flag")
    void deleteTransaction_existing_marksAsDeleted() {
        when(transactionRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        transactionService.deleteTransaction(1L);

        assertThat(testTransaction.isDeleted()).isTrue();
        assertThat(testTransaction.getDeletedAt()).isNotNull();
        verify(transactionRepository).save(testTransaction);
    }
}
