package com.zuply.modules.user.repository;

import com.zuply.common.enums.Role;
import com.zuply.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    long countByRole(Role role);
    java.util.List<User> findByRole(Role role);
}