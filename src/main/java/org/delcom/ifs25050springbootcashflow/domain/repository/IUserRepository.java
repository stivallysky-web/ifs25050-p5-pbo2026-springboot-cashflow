package org.delcom.ifs25050springbootcashflow.domain.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}