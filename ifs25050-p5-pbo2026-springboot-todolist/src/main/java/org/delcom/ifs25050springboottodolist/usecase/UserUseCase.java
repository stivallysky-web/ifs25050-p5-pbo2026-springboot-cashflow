package org.delcom.ifs25050springbootcashflow.usecase;

import org.delcom.ifs25050springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25050springbootcashflow.domain.entity.User;
import org.delcom.ifs25050springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25050springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050springbootcashflow.domain.repository.IUserRepository;

import java.util.List;
import java.util.Optional;

public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateProfile(Long id, String name, String photoPath) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong.");
        }
        User user = findById(id);
        user.setName(name.trim());
        if (photoPath != null) {
            user.setPhotoPath(photoPath);
        }
        return userRepository.save(user);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password baru minimal 6 karakter.");
        }
        User user = findById(id);
        if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
            throw new ValidationException("Password lama salah.");
        }
        user.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepository.save(user);
    }
}