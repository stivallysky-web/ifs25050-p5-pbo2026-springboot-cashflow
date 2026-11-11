package org.delcom.ifs25050springbootcashflow.usecase;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25050springbootcashflow.domain.exception.ValidationException;

import java.util.List;

public class TodoUseCase {

    private final ITodoRepository todoRepository;

    public TodoUseCase(ITodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Todo create(Long userId, String title, String description, String coverPath) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Judul todo tidak boleh kosong.");
        }
        Todo todo = new Todo(null, userId, title.trim(), description != null ? description.trim() : "", false, coverPath, java.time.LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Todo", id));
    }

    public List<Todo> findByUserId(Long userId) {
        return todoRepository.findByUserId(userId);
    }

    public List<Todo> search(Long userId, String keyword, SortOption sortOption) {
        return todoRepository.search(userId, keyword, sortOption != null ? sortOption : SortOption.NEWEST);
    }

    public Todo update(Long id, Long requestingUserId, String title, String description, String coverPath) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Judul todo tidak boleh kosong.");
        }
        Todo todo = findById(id);
        if (!todo.getUserId().equals(requestingUserId)) {
            throw new ValidationException("Anda tidak berhak mengubah todo ini.");
        }
        todo.setTitle(title.trim());
        todo.setDescription(description != null ? description.trim() : "");
        if (coverPath != null) {
            todo.setCoverPath(coverPath);
        }
        return todoRepository.save(todo);
    }

    public Todo toggleCompleted(Long id, Long requestingUserId) {
        Todo todo = findById(id);
        if (!todo.getUserId().equals(requestingUserId)) {
            throw new ValidationException("Anda tidak berhak mengubah status todo ini.");
        }
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    public void delete(Long id, Long requestingUserId) {
        Todo todo = findById(id);
        if (!todo.getUserId().equals(requestingUserId)) {
            throw new ValidationException("Anda tidak berhak menghapus todo ini.");
        }
        todoRepository.deleteById(id);
    }
}