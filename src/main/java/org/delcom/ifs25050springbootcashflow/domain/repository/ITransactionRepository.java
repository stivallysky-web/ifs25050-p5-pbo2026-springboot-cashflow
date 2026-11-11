package org.delcom.ifs25050springbootcashflow.domain.repository;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.TransactionType;

import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption);
    Double getTotalIncome(Long userId);
    Double getTotalExpense(Long userId);
    Double getBalance(Long userId);
}