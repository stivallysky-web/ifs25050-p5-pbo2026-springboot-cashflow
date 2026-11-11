package org.delcom.ifs25050springbootcashflow.adapter.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
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

public class JdbcTodoRepository implements ITodoRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Todo> rowMapper = (rs, rowNum) -> new Todo(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getBoolean("completed"),
            rs.getString("cover_path"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : LocalDateTime.now()
    );

    @Override
    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            String sql = "INSERT INTO todos (user_id, title, description, completed, cover_path, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, todo.getUserId());
                ps.setString(2, todo.getTitle());
                ps.setString(3, todo.getDescription());
                ps.setBoolean(4, todo.isCompleted());
                ps.setString(5, todo.getCoverPath());
                ps.setTimestamp(6, Timestamp.valueOf(todo.getCreatedAt() != null ? todo.getCreatedAt() : LocalDateTime.now()));
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
                todo.setId(((Number) keyHolder.getKeys().get("id")).longValue());
            } else if (keyHolder.getKey() != null) {
                todo.setId(keyHolder.getKey().longValue());
            }
            return todo;
        } else {
            String sql = "UPDATE todos SET title = ?, description = ?, completed = ?, cover_path = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    todo.getTitle(),
                    todo.getDescription(),
                    todo.isCompleted(),
                    todo.getCoverPath(),
                    todo.getId());
            return todo;
        }
    }

    @Override
    public Optional<Todo> findById(Long id) {
        String sql = "SELECT * FROM todos WHERE id = ?";
        List<Todo> todos = jdbcTemplate.query(sql, rowMapper, id);
        return todos.stream().findFirst();
    }

    @Override
    public List<Todo> findAll() {
        String sql = "SELECT * FROM todos ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM todos WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM todos WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Todo> findByUserId(Long userId) {
        String sql = "SELECT * FROM todos WHERE user_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    @Override
    public List<Todo> search(Long userId, String keyword, SortOption sortOption) {
        StringBuilder sql = new StringBuilder("SELECT * FROM todos WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

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
            case STATUS_INCOMPLETE_FIRST -> " ORDER BY completed ASC, id DESC";
            case STATUS_COMPLETED_FIRST -> " ORDER BY completed DESC, id DESC";
        };
        sql.append(orderBy);

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }
}