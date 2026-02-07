package com.library.loans;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByMemberIdAndReturnedAtIsNull(UUID memberId);

    List<Loan> findByBookId(UUID bookId);

    List<Loan> findByDueDateBeforeAndReturnedAtIsNull(LocalDate date);

    List<Loan> findByBookIdAndReturnedAtIsNull(UUID bookId);

    long countByMemberIdAndReturnedAtIsNull(UUID memberId);

    long countByBookIdAndReturnedAtIsNull(UUID bookId);

    long countByMemberIdAndBookIdAndReturnedAtIsNull(UUID memberId, UUID bookID);

    Optional<Loan> findByMemberIdAndBookIdAndReturnedAtIsNull(UUID memberId, UUID bookId);
}