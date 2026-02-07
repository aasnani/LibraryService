package com.library.loans;

import com.library.books.Book;
import com.library.members.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long getActiveLoanCountForMember(@Nonnull UUID memberId) {
        return loanRepository.countByMemberIdAndReturnedAtIsNull(memberId);
    }

    @Transactional(readOnly = true)
    public long getActiveLoanCountForBook(@Nonnull UUID bookId) {
        return loanRepository.countByBookIdAndReturnedAtIsNull(bookId);
    }

    @Transactional(readOnly = true)
    public long getActiveLoanCountForBookAndMember(@Nonnull UUID bookId, @Nonnull UUID memberId) {
        return loanRepository.countByMemberIdAndBookIdAndReturnedAtIsNull(memberId, bookId);
    }

    @Transactional
    public Loan createLoanRecord(@NonNull Member member, @NonNull Book book, int daysUntilDue) {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID()); // Explicitly setting ID for the save
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowedAt(OffsetDateTime.now());

        // Match the Migration: due_date logic (e.g., 14 day loan period)
        loan.setDueDate(LocalDate.now().plusDays(daysUntilDue));

        loan.setReturnedAt(null);
        return loanRepository.save(loan);
    }

    @Transactional
    public void closeLoanRecord(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        Loan loan = loanRepository.findByMemberIdAndBookIdAndReturnedAtIsNull(memberId, bookId)
                .orElseThrow(() -> new IllegalStateException("No active loan found for this member and book"));

        loan.setReturnedAt(OffsetDateTime.now());
        loanRepository.save(loan);
    }
}