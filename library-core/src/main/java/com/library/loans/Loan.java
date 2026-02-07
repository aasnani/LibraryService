package com.library.loans;

import com.library.books.Book;
import com.library.common.BaseEntity;
import com.library.members.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Represents a borrowing transaction where a {@link Member} takes a {@link Book}
 * from the library for a specified period.
 *
 * <p>This entity tracks the lifecycle of a loan, including the initial checkout,
 * the expected return date, and the actual completion of the transaction.</p>
 *
 * <p>Optimized for performance via indexes on member-specific active loans,
 * due dates for late-return processing, and creation timestamps for auditing.</p>
 */
@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_active_loans_member", columnList = "member_id, returned_at"),
    @Index(name = "idx_loan_due_date", columnList = "due_date"),
    @Index(name = "idx_loan_created_at", columnList = "created_at")
})
@Getter
@Setter
public class Loan extends BaseEntity {

    /**
     * The library member who is borrowing the book.
     * Fetched lazily to optimize performance when loading loan lists.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * The specific book being borrowed.
     * Fetched lazily to prevent unnecessary joins during bulk loan processing.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /**
     * The exact moment the loan was initiated.
     * Uses timezone-aware storage to ensure audit accuracy across regions.
     */
    @Column(nullable = false)
    private OffsetDateTime borrowedAt;

    /**
     * The date by which the book is expected to be returned.
     * Stored as a date only, as library policies are typically based on calendar days.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * The moment the book was returned to the library.
     * If null, the loan is considered "active" or "outstanding."
     */
    @Column(nullable = true)
    private OffsetDateTime returnedAt;
}