package com.finance.dashboard.repository;

import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<User> findByIdAndDeletedFalse(Long id);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    Page<User> findAllByRoleAndDeletedFalse(Role role, Pageable pageable);

    Page<User> findAllByStatusAndDeletedFalse(UserStatus status, Pageable pageable);

    long countByDeletedFalse();

    long countByRoleAndDeletedFalse(Role role);
}
