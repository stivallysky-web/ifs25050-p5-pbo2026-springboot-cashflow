package org.delcom.ifs25050springbootcashflow.adapter.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050springbootcashflow.domain.repository.ITransactionRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryTransactionRepository implements ITransactionRepository {

    private final Map<Long, Transaction> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(idGenerator.getAndIncrement());
        }
        storage.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Transaction> findAll() {
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
    public List<Transaction> findByUserId(Long userId) {
        return storage.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        var stream = storage.values().stream()
                .filter(t -> t.getUserId().equals(userId));

        if (type != null) {
            stream = stream.filter(t -> t.getType() == type);
        }

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            stream = stream.filter(t -> t.getTitle().toLowerCase().contains(lower)
                    || (t.getDescription() != null && t.getDescription().toLowerCase().contains(lower)));
        }

        Comparator<Transaction> comparator = switch (sortOption != null ? sortOption : SortOption.NEWEST) {
            case NEWEST -> Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            case OLDEST -> Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case TITLE_ASC -> Comparator.comparing(t -> t.getTitle().toLowerCase());
            case TITLE_DESC -> Comparator.comparing((Transaction t) -> t.getTitle().toLowerCase()).reversed();
            case AMOUNT_HIGH -> Comparator.comparing(Transaction::getAmount).reversed();
            case AMOUNT_LOW -> Comparator.comparing(Transaction::getAmount);
        };

        return stream.sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public Double getTotalIncome(Long userId) {
        return storage.values().stream()
                .filter(t -> t.getUserId().equals(userId) && t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public Double getTotalExpense(Long userId) {
        return storage.values().stream()
                .filter(t -> t.getUserId().equals(userId) && t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public Double getBalance(Long userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }
}