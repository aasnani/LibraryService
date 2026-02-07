package com.library.loans;

import com.library.books.Book;
import com.library.common.BaseEntity;
import com.library.members.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans", indexes = {
    @Index(name = "idx_active_loans_member", columnList = "member_id, returnedAt"),
    @Index(name = "idx_loan_due_date", columnList = "dueDate"),
    @Index(name = "idx_loan_created_at", columnList = "createdAt")
})
@Getter
@Setter
public class Loan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime borrowedAt;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDateTime returnedAt;
}