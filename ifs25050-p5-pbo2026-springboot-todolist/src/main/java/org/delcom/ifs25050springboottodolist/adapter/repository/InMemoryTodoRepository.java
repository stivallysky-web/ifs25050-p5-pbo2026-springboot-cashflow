package org.delcom.ifs25050springbootcashflow.adapter.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryTodoRepository implements ITodoRepository {

    private final Map<Long, Todo> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            todo.setId(idGenerator.getAndIncrement());
        }
        storage.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public List<Todo> findByUserId(Long userId) {
        return storage.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Todo> search(Long userId, String keyword, SortOption sortOption) {
        var stream = storage.values().stream()
                .filter(t -> t.getUserId().equals(userId));

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            stream = stream.filter(t -> t.getTitle().toLowerCase().contains(lower)
                    || (t.getDescription() != null && t.getDescription().toLowerCase().contains(lower)));
        }

        Comparator<Todo> comparator = switch (sortOption != null ? sortOption : SortOption.NEWEST) {
            case NEWEST -> Comparator.comparing(Todo::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            case OLDEST -> Comparator.comparing(Todo::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case TITLE_ASC -> Comparator.comparing(t -> t.getTitle().toLowerCase());
            case TITLE_DESC -> Comparator.comparing((Todo t) -> t.getTitle().toLowerCase()).reversed();
            case STATUS_INCOMPLETE_FIRST -> Comparator.comparing(Todo::isCompleted);
            case STATUS_COMPLETED_FIRST -> Comparator.comparing(Todo::isCompleted).reversed();
        };

        return stream.sorted(comparator).collect(Collectors.toList());
    }
}