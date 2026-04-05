package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.TransactionFilterRequest;
import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.TransactionResponse;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.TransactionSpecification;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public PagedResponse<TransactionResponse> getTransactions(TransactionFilterRequest filter) {
        // validate date range if both are present
        if (filter.getFrom() != null && filter.getTo() != null
                && filter.getFrom().isAfter(filter.getTo())) {
            throw new BadRequestException("'from' date cannot be after 'to' date");
        }

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Transaction> spec = TransactionSpecification.buildFilter(
                filter.getType(),
                filter.getCategory(),
                filter.getFrom(),
                filter.getTo(),
                filter.getKeyword()
        );

        Page<Transaction> result = transactionRepository.findAll(spec, pageable);
        return PagedResponse.of(result.map(TransactionResponse::from));
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction tx = findActiveOrThrow(id);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = getCurrentUser();

        Transaction tx = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        tx = transactionRepository.save(tx);
        log.info("Transaction created: id={} by user={}", tx.getId(), currentUser.getEmail());
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction tx = findActiveOrThrow(id);

        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setCategory(request.getCategory());
        tx.setDate(request.getDate());
        tx.setDescription(request.getDescription());
        tx.setNotes(request.getNotes());

        tx = transactionRepository.save(tx);
        log.info("Transaction updated: id={}", id);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction tx = findActiveOrThrow(id);
        tx.setDeleted(true);
        tx.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(tx);
        log.info("Transaction soft-deleted: id={}", id);
    }

    // ---------- helpers ----------

    private Transaction findActiveOrThrow(Long id) {
        return transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
