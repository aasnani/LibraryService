package com.library.loans;

import com.library.books.Book;
import com.library.books.BookRepository;
import com.library.common.BaseRepositoryTest;
import com.library.members.Member;
import com.library.members.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRepositoryTest extends BaseRepositoryTest {

    @Autowired private LoanRepository loanRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;

    @Test
    @Transactional
    @DisplayName("Counts active loans for a member")
    void shouldCountActiveLoansForMember() {
        Member member = createAndSaveMember("Active", "Borrower", "active@test.com");
        Book book = createAndSaveBook("Clean Code", "9780132350884");

        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(14), null);

        long count = loanRepository.countByMemberIdAndReturnedAtIsNull(member.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("Counts active loans for a book")
    void shouldCountActiveLoansForBook() {
        Member member = createAndSaveMember("John", "Doe", "john@test.com");
        Book book = createAndSaveBook("Refactoring", "9780134757599");

        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(7), null);

        long count = loanRepository.countByBookIdAndReturnedAtIsNull(book.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("Counts active loans for a book and member pair")
    void shouldCountActiveLoansForBookAndMember() {
        Member member = createAndSaveMember("Jane", "Smith", "jane@test.com");
        Book book = createAndSaveBook("DDD", "9780321125217");

        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(10), null);

        long count = loanRepository.countByMemberIdAndBookIdAndReturnedAtIsNull(
                member.getId(), book.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("Returns aggregated loan stats for a member")
    void shouldReturnLoanStatsProjection() {
        Member member = createAndSaveMember("Late", "User", "late@test.com");
        Book book1 = createAndSaveBook("Database Internals", "9781492040347");
        Book book2 = createAndSaveBook("Designing Data-Intensive Apps", "9781449373320");

        // Active, overdue
        saveLoan(
                member,
                book1,
                OffsetDateTime.now().minusDays(20),
                LocalDate.now().minusDays(5),
                null
        );

        // Active, not overdue
        saveLoan(
                member,
                book2,
                OffsetDateTime.now(),
                LocalDate.now().plusDays(7),
                null
        );

        LoanStatsProjection stats =
                loanRepository.getLoanStatsForMember(member.getId(), book1.getId());

        assertThat(stats.getActiveLoans()).isEqualTo(2);
        assertThat(stats.getOverdueLoans()).isEqualTo(1);
        assertThat(stats.getHasThisBook()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("Closes an active loan and returns affected row count")
    void shouldCloseLoanRecord() {
        Member member = createAndSaveMember("Return", "User", "return@test.com");
        Book book = createAndSaveBook("Clean Architecture", "9780134494166");

        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(14), null);

        long closed = loanRepository.closeLoanRecord(member.getId(), book.getId());

        assertThat(closed).isEqualTo(1);

        long remaining =
                loanRepository.countByMemberIdAndBookIdAndReturnedAtIsNull(
                        member.getId(), book.getId());

        assertThat(remaining).isEqualTo(0);
    }

    // --- Helpers ---

    private Member createAndSaveMember(String first, String last, String email) {
        Member m = new Member();
        m.setFirstName(first);
        m.setLastName(last);
        m.setEmail(email);
        return memberRepository.saveAndFlush(m);
    }

    private Book createAndSaveBook(String title, String isbn) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor("Sample Author");
        b.setIsbn(isbn);
        b.setTotalCopies(1);
        b.setAvailableCopies(1);
        return bookRepository.saveAndFlush(b);
    }

    private Loan saveLoan(
            Member member,
            Book book,
            OffsetDateTime borrowed,
            LocalDate due,
            OffsetDateTime returned
    ) {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowedAt(borrowed);
        loan.setDueDate(due);
        loan.setReturnedAt(returned);
        return loanRepository.saveAndFlush(loan);
    }
}
