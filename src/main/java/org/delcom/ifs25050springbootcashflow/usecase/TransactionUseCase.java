package org.delcom.ifs25050springbootcashflow.usecase;

import org.delcom.ifs25050springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25050springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25050springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25050springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25050springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25050springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionUseCase {

    private final ITransactionRepository transactionRepository;

    public TransactionUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(Long userId, String title, String description,
                              TransactionType type, Double amount, String attachmentPath) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Judul transaksi tidak boleh kosong.");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi (Pemasukan/Pengeluaran) harus dipilih.");
        }
        if (amount == null || amount <= 0) {
            throw new ValidationException("Nominal transaksi harus lebih besar dari 0.");
        }

        Transaction transaction = new Transaction(
                null, userId, title.trim(),
                description != null ? description.trim() : "",
                type, amount, attachmentPath, LocalDateTime.now()
        );
        return transactionRepository.save(transaction);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", id));
    }

    public List<Transaction> findByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        return transactionRepository.search(userId, keyword, type, sortOption != null ? sortOption : SortOption.NEWEST);
    }

    public Transaction update(Long id, Long requestingUserId, String title,
                              String description, TransactionType type, Double amount, String attachmentPath) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Judul transaksi tidak boleh kosong.");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi harus dipilih.");
        }
        if (amount == null || amount <= 0) {
            throw new ValidationException("Nominal transaksi harus lebih besar dari 0.");
        }

        Transaction transaction = findById(id);
        if (!transaction.getUserId().equals(requestingUserId)) {
            throw new ValidationException("Anda tidak berhak mengubah transaksi ini.");
        }

        transaction.setTitle(title.trim());
        transaction.setDescription(description != null ? description.trim() : "");
        transaction.setType(type);
        transaction.setAmount(amount);
        if (attachmentPath != null) {
            transaction.setAttachmentPath(attachmentPath);
        }

        return transactionRepository.save(transaction);
    }

    public void delete(Long id, Long requestingUserId) {
        Transaction transaction = findById(id);
        if (!transaction.getUserId().equals(requestingUserId)) {
            throw new ValidationException("Anda tidak berhak menghapus transaksi ini.");
        }
        transactionRepository.deleteById(id);
    }

    public Double getTotalIncome(Long userId) {
        return transactionRepository.getTotalIncome(userId);
    }

    public Double getTotalExpense(Long userId) {
        return transactionRepository.getTotalExpense(userId);
    }

    public Double getBalance(Long userId) {
        return transactionRepository.getBalance(userId);
    }
}