package com.library.loans;

import com.library.books.Book;
import com.library.members.Member;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing the lifecycle of book loans.
 * Handles creation, closure, and inventory-related calculations for library
 * transactions.
 */
@Service
@Validated
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    /**
     * Retrieves all loan records from the database.
     *
     * @return a list of all {@link Loan} entities.
     */
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Counts the number of active (unreturned) loans associated with a specific
     * member.
     *
     * @param memberId the unique identifier of the member.
     *
     * @return the total count of active loans for the member.
     */
    @Transactional(readOnly = true)
    public long getActiveLoanCountForMember(@Nonnull UUID memberId) {
        return loanRepository.countByMemberIdAndReturnedAtIsNull(memberId);
    }

    /**
     * Counts the total number of active (unreturned) loans for a specific book
     * across all members.
     *
     * @param bookId the unique identifier of the book.
     *
     * @return the total count of copies currently checked out.
     */
    @Transactional(readOnly = true)
    public long getActiveLoanCountForBook(@Nonnull UUID bookId) {
        return loanRepository.countByBookIdAndReturnedAtIsNull(bookId);
    }

    /**
     * Creates and persists a new loan record.
     * Sets the borrowed timestamp to the current time and calculates the due date.
     *
     * @param member       the member borrowing the book.
     *
     * @param book         the book being borrowed.
     * @param daysUntilDue the number of days allowed before the book must be
     *                     returned.
     * @return the persisted {@link Loan} entity.
     */
    @Transactional
    public Loan createLoanRecord(@Nonnull Member member, @Nonnull Book book, int daysUntilDue) {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowedAt(OffsetDateTime.now());

        loan.setDueDate(LocalDate.now().plusDays(daysUntilDue));

        loan.setReturnedAt(null);
        return loanRepository.save(loan);
    }

    /**
     * Retrieves aggregate loan statistics for a member in a single database query.
     *
     * <p>
     * This method returns:
     * <ul>
     * <li>The number of active (not yet returned) loans for the member</li>
     * <li>The number of overdue loans based on calendar-date comparison</li>
     * <li>Whether the member currently has an active loan for the specified
     * book</li>
     * </ul>
     *
     * <p>
     * The underlying query is optimized to avoid entity hydration and multiple
     * round-trips, making it suitable for enforcing borrowing rules such as
     * maximum active loans, overdue restrictions, and duplicate checkouts.
     *
     * @param memberId the unique identifier of the member whose loan statistics are
     *                 requested
     * @param bookId   the unique identifier of the book to check for an existing
     *                 active loan
     * @return a {@link LoanStatsProjection} containing aggregated loan statistics
     *         for the member
     */
    @Transactional
    public LoanStatsProjection getLoanStatsProjection(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        return loanRepository.getLoanStatsForMember(memberId, bookId);
    }

    @Transactional
    public int closeLoanRecord(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        return loanRepository.closeLoanRecord(memberId, bookId);
    }

    /**
     * Retrieves a paginated list of all loans.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoans(@Nonnull Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    /**
     * Retrieves a paginated list of loans for a specific member.
     *
     * @param memberId the UUID of the member
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities for the member
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByMember(UUID memberId, Pageable pageable) {
        return loanRepository.findByMember_Id(memberId, pageable);
    }

    /**
     * Retrieves a paginated list of loans for a specific book.
     *
     * @param bookId   the UUID of the book
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities for the book
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByBook(UUID bookId, Pageable pageable) {
        return loanRepository.findByBook_Id(bookId, pageable);
    }
}