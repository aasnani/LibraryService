package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.config.LoanPolicyProperties;
import com.library.members.Member;
import com.library.members.MemberService;
import com.library.loans.Loan;
import com.library.loans.LoanService;
import com.library.loans.LoanStatsProjection;

import jakarta.annotation.Nonnull;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

/**
 * Facade and orchestration layer for core library operations.
 *
 * <p>
 * This service coordinates interactions between books, members, and loans,
 * enforcing borrowing policies and transactional consistency while delegating
 * persistence and domain-specific logic to underlying services.
 */
@Service
@Validated
@RequiredArgsConstructor
public class LibraryService {

    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;
    private final LoanPolicyProperties policy;

    /**
     * Checks out a book for a member, enforcing inventory and loan policy rules.
     *
     * <p>
     * This operation validates:
     * <ul>
     * <li>The book has available copies</li>
     * <li>The member has not exceeded the maximum number of active loans</li>
     * <li>The member does not already have the same book checked out</li>
     * <li>The member is not blocked due to overdue loans</li>
     * </ul>
     *
     * <p>
     * If all checks pass, the book inventory is decremented and a new loan
     * record is created. The entire operation is executed within a single
     * transaction to ensure consistency.
     *
     * @param memberId the unique identifier of the borrowing member
     * @param bookId   the unique identifier of the book to be checked out
     * @return the created {@link Loan}
     * @throws IllegalStateException if any borrowing rule is violated
     */
    @Transactional
    public Loan checkoutBook(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        Member member = memberService.getMemberById(memberId);
        Book book = bookService.getBookById(bookId);

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for: " + book.getIsbn());
        }

        LoanStatsProjection stats = loanService.getLoanStatsProjection(memberId, bookId);

        if (stats.getActiveLoans() >= policy.getMaxActive()) {
            throw new IllegalStateException(
                    String.format("Member has reached the maximum loan limit (%d)", policy.getMaxActive()));
        }

        if (stats.getHasThisBook()) {
            throw new IllegalStateException("Member already has this book checked out");
        }

        if (stats.getOverdueLoans() > policy.getOverdueBlockThreshold()) {
            throw new IllegalStateException(
                    String.format(
                            "Member has %d overdue loans and must return %d books before they can borrow another",
                            stats.getOverdueLoans(),
                            stats.getOverdueLoans(),
                            -policy.getOverdueBlockThreshold()));
        }

        long updated = bookService.decrementAvailableCopies(bookId);
        if (updated == 0) {
            throw new IllegalStateException("No copies available for: " + book.getIsbn());
        }

        return loanService.createLoanRecord(member, book, policy.getLoanDurationDays());
    }

    /**
     * Returns a borrowed book by closing the active loan and updating inventory.
     *
     * <p>
     * The method fails fast if no active loan exists for the given member and
     * book, preventing inventory corruption.
     *
     * <p>
     * This operation is transactional to guarantee that loan closure and
     * inventory updates are applied atomically.
     *
     * @param memberId the unique identifier of the member returning the book
     * @param bookId   the unique identifier of the book being returned
     * @throws IllegalStateException if no active loan is found to return
     */
    @Transactional
    public void returnBook(UUID memberId, UUID bookId) {
        long closed = loanService.closeLoanRecord(memberId, bookId);
        if (closed == 0) {
            throw new IllegalStateException("No active loan found to return");
        }

        bookService.incrementAvailableCopies(bookId);
    }

    /**
     * Retrieves all active books in the library.
     *
     * @return a list of active {@link Book} records
     */
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    /**
     * Retrieves a single book by its identifier.
     *
     * @param id the unique identifier of the book
     * @return the corresponding {@link Book}
     */
    public Book getBook(UUID id) {
        return bookService.getBookById(id);
    }

    /**
     * Adds a new book to the library.
     *
     * @param book the book to create
     * @return the created {@link Book}
     */
    public Book addBook(Book book) {
        return bookService.createBook(book);
    }

    /**
     * Updates an existing book in the library.
     * @param id the id of the book to update
     * @param updatedBook the updated book
     * @return the updated {@link Book}
     */
    public Book updateBook(UUID bookId, Book updatedBook) {
        long activeLoans = loanService.getActiveLoanCountForBook(bookId);

       return  bookService.updateBook(bookId, updatedBook, activeLoans);
    }

    /**
     * Removes a book from the library.
     *
     * <p>
     * If the book has active loans, deletion behavior is delegated to the
     * underlying service.
     *
     * @param id the unique identifier of the book to remove
     */
    @Transactional
    public void removeBook(UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForBook(id) > 0;
        bookService.deleteBook(id, hasLoans);
    }

    /**
     * Retrieves all registered members.
     *
     * @return a list of {@link Member} records
     */
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    /**
     * Retrieves a member by identifier.
     *
     * @param id the unique identifier of the member
     * @return the corresponding {@link Member}
     */
    public Member getMember(UUID id) {
        return memberService.getMemberById(id);
    }

    /**
     * Get a member by email.
     *
     * @param email the email of the member
     * @return the corresponding {@link Member}
     */
    public Member getMemberByEmail(String email) {
        return memberService.getMemberByEmail(email);
    }

    /**
     * Get a list of members by name.
     *
     * @param firstName the first name search term
     * @param lastName  the last name search term
     * @return a list of {@link Member} that match the search query
     */
    public List<Member> getMembersByName(String firstName, String lastName) {
        return memberService.getMembersByName(firstName, lastName);
    }

    /**
     * Registers a new library member.
     *
     * @param member the member to create
     * @return the created {@link Member}
     */
    public Member registerMember(Member member) {
        return memberService.createMember(member);
    }

    /**
     * Updates an existing member in the library.
     * @param id the id of the member to update
     * @param updatedBook the updated member
     * @return the updated {@link Member}
     */
    public Member updateMember(UUID memberId, Member updatedMember) {
        return memberService.updateMember(memberId, updatedMember);
    }

    /**
     * Removes a member from the library.
     *
     * <p>
     * If the member has active loans, deletion behavior is delegated to the
     * underlying service.
     *
     * @param id the unique identifier of the member to remove
     */
    @Transactional
    public void removeMember(UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForMember(id) > 0;
        memberService.deleteMember(id, hasLoans);
    }

    /**
     * Returns a paginated list of all loans.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities
     */
    public Page<Loan> getLoans(Pageable pageable) {
        return loanService.getLoans(pageable);
    }

    /**
     * Returns a paginated list of loans for a specific member.
     *
     * @param memberId the UUID of the member
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities for the member
     */
    public Page<Loan> getLoansByMember(UUID memberId, Pageable pageable) {
        return loanService.getLoansByMember(memberId, pageable);
    }

    /**
     * Returns a paginated list of loans for a specific book.
     *
     * @param bookId   the UUID of the book
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities for the book
     */
    public Page<Loan> getLoansByBook(UUID bookId, Pageable pageable) {
        return loanService.getLoansByBook(bookId, pageable);
    }

    /**
     * Returns a paginated list of all books.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Book} entities
     */
    public Page<Book> getBooks(Pageable pageable) {
        return bookService.getBooks(pageable);
    }

    /**
     * Returns a paginated list of all members.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Member} entities
     */
    public Page<Member> getMembers(Pageable pageable) {
        return memberService.getMembers(pageable);
    }

}
