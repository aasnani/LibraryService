package com.library.loans;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByMemberIdAndReturnedAtIsNull(UUID memberId);

    List<Loan> findByBookId(UUID bookId);

    List<Loan> findByDueDateBeforeAndReturnedAtIsNull(LocalDate date);
}