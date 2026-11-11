package org.delcom.ifs25050springbootcashflow.domain.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;

import java.util.List;

public interface ITodoRepository extends IRepository<Todo, Long> {
    List<Todo> findByUserId(Long userId);
    List<Todo> search(Long userId, String keyword, SortOption sortOption);
}