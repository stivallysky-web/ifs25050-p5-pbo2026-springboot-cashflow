package org.delcom.ifs25050springbootcashflow.adapter.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransactionRepository implements ITransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> new Transaction(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getDouble("amount"),
            rs.getString("attachment_path"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : LocalDateTime.now()
    );

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            String sql = "INSERT INTO transactions (user_id, title, description, type, amount, attachment_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, transaction.getUserId());
                ps.setString(2, transaction.getTitle());
                ps.setString(3, transaction.getDescription());
                ps.setString(4, transaction.getType().name());
                ps.setDouble(5, transaction.getAmount());
                ps.setString(6, transaction.getAttachmentPath());
                ps.setTimestamp(7, Timestamp.valueOf(transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now()));
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
                transaction.setId(((Number) keyHolder.getKeys().get("id")).longValue());
            } else if (keyHolder.getKey() != null) {
                transaction.setId(keyHolder.getKey().longValue());
            }
            return transaction;
        } else {
            String sql = "UPDATE transactions SET title = ?, description = ?, type = ?, amount = ?, attachment_path = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    transaction.getTitle(),
                    transaction.getDescription(),
                    transaction.getType().name(),
                    transaction.getAmount(),
                    transaction.getAttachmentPath(),
                    transaction.getId());
            return transaction;
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        List<Transaction> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    @Override
    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?))");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        String orderBy = switch (sortOption != null ? sortOption : SortOption.NEWEST) {
            case NEWEST -> " ORDER BY id DESC";
            case OLDEST -> " ORDER BY id ASC";
            case TITLE_ASC -> " ORDER BY LOWER(title) ASC";
            case TITLE_DESC -> " ORDER BY LOWER(title) DESC";
            case AMOUNT_HIGH -> " ORDER BY amount DESC, id DESC";
            case AMOUNT_LOW -> " ORDER BY amount ASC, id DESC";
        };
        sql.append(orderBy);

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public Double getTotalIncome(Long userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE user_id = ? AND type = 'INCOME'";
        Double total = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalExpense(Long userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE user_id = ? AND type = 'EXPENSE'";
        Double total = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getBalance(Long userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }
}