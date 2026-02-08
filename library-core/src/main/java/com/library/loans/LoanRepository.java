package com.library.loans;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

        long countByMemberIdAndReturnedAtIsNull(UUID memberId);

        long countByBookIdAndReturnedAtIsNull(UUID bookId);

        long countByMemberIdAndBookIdAndReturnedAtIsNull(UUID memberId, UUID bookID);

        Page<Loan> findByMember_Id(UUID memberId, Pageable pageable);

        Page<Loan> findByBook_Id(UUID bookId, Pageable pageable);

        @Query(value = """
                        SELECT
                            COUNT(*) FILTER (WHERE returned_at IS NULL) AS activeLoans,
                            COUNT(*) FILTER (WHERE returned_at IS NULL AND due_date < CURRENT_DATE) AS overdueLoans,
                            COALESCE(BOOL_OR(book_id = :bookId AND returned_at IS NULL), FALSE) AS hasThisBook
                        FROM loans
                        WHERE member_id = :memberId

                                                """, nativeQuery = true)
        LoanStatsProjection getLoanStatsForMember(
                        @Param("memberId") UUID memberId,
                        @Param("bookId") UUID bookId);

        @Modifying
        @Transactional
        @Query("""
                            UPDATE Loan l
                            SET l.returnedAt = CURRENT_TIMESTAMP
                            WHERE l.member.id = :memberId
                              AND l.book.id = :bookId
                              AND l.returnedAt IS NULL
                        """)
        int closeLoanRecord(
                        @Param("memberId") UUID memberId,
                        @Param("bookId") UUID bookId);

}