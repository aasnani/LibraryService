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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRepositoryTest extends BaseRepositoryTest {

    @Autowired private LoanRepository loanRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BookRepository bookRepository;

    @Test
    @Transactional
    @DisplayName("Should find active loans for a specific member")
    void shouldFindActiveLoansByMember() {
        Member member = createAndSaveMember("Active", "Borrower", "active@test.com", 100L);
        Book book = createAndSaveBook("Clean Code", "9780132350884", 500L);

        // Save one active loan (returnedAt is null)
        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(14), null);

        List<Loan> activeLoans = loanRepository.findByMemberIdAndReturnedAtIsNull(member.getId());

        assertThat(activeLoans).hasSize(1);
        assertThat(activeLoans.get(0).getBook().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @Transactional
    @DisplayName("Should find all loans associated with a specific book")
    void shouldFindByBookId() {
        Member member = createAndSaveMember("John", "Doe", "john@test.com", 101L);
        Book book = createAndSaveBook("Refactoring", "9780134757599", 501L);

        saveLoan(member, book, OffsetDateTime.now(), LocalDate.now().plusDays(7), OffsetDateTime.now());

        List<Loan> bookHistory = loanRepository.findByBookId(book.getId());

        assertThat(bookHistory).hasSize(1);
        assertThat(bookHistory.get(0).getMember().getLastName()).isEqualTo("Doe");
    }

    @Test
    @Transactional
    @DisplayName("Should find overdue loans that have not been returned")
    void shouldFindOverdueUnreturnedLoans() {
        Member member = createAndSaveMember("Late", "User", "late@test.com", 102L);
        Book book = createAndSaveBook("Database Internals", "9781492040347", 502L);

        // Loan was due yesterday
        saveLoan(member, book, OffsetDateTime.now().minusDays(15), LocalDate.now().minusDays(1), null);

        List<Loan> overdueLoans = loanRepository.findByDueDateBeforeAndReturnedAtIsNull(LocalDate.now());

        assertThat(overdueLoans).hasSize(1);
        assertThat(overdueLoans.get(0).getMember().getFirstName()).isEqualTo("Late");
    }

    // --- Helpers ---

    private Member createAndSaveMember(String first, String last, String email, Long num) {
        Member m = new Member();
        m.setFirstName(first);
        m.setLastName(last);
        m.setEmail(email);
        m.setMembershipNumber(num);
        return memberRepository.saveAndFlush(m);
    }

    private Book createAndSaveBook(String title, String isbn, Long num) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor("Sample Author");
        b.setIsbn(isbn);
        b.setBookNumber(num);
        b.setTotalCopies(1);
        b.setAvailableCopies(1);
        return bookRepository.saveAndFlush(b);
    }

    private Loan saveLoan(Member member, Book book, OffsetDateTime borrowed, LocalDate due, OffsetDateTime returned) {
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowedAt(borrowed);
        loan.setDueDate(due);
        loan.setReturnedAt(returned);
        return loanRepository.saveAndFlush(loan);
    }
}