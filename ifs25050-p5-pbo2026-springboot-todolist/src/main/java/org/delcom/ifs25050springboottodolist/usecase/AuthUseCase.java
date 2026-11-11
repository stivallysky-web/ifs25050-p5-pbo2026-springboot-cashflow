package org.delcom.ifs25050springbootcashflow.usecase;

import org.delcom.ifs25050springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25050springbootcashflow.domain.entity.Role;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class AuthUseCase {

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String name, String email, String password, Role role) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong.");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email tidak boleh kosong.");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password minimal 6 karakter.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email sudah terdaftar.");
        }

        String passwordHash = PasswordHasher.hash(password);
        User user = new User(name.trim(), email.trim().toLowerCase(), passwordHash, role != null ? role : Role.USER);
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> PasswordHasher.verify(password, u.getPasswordHash()));
    }
}