package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.config.LoanPolicyProperties;
import com.library.exceptions.LibraryException;
import com.library.exceptions.LibraryException.LibraryExceptionType;
import com.library.members.Member;
import com.library.members.MemberService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import com.library.loans.Loan;
import com.library.loans.LoanService;
import com.library.loans.LoanStatsProjection;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

/**
 * Facade and orchestration layer for core library operations.
 *
 * <p>
 * Coordinates interactions between books, members, and loans, enforcing
 * borrowing policies and transactional consistency while delegating
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

    // Inject MeterRegistry without modifying constructor
    @Autowired
    private MeterRegistry meterRegistry;

    // Example counters
    private Counter booksCheckedOut;
    private Counter booksReturned;

    @PostConstruct
    private void initMetrics() {
        booksCheckedOut = meterRegistry.counter("library.books.checkedout.total");
        booksReturned = meterRegistry.counter("library.books.returned.total");
    }

    // ----------------------------------------
    // Books
    // ----------------------------------------

    /**
     * Retrieves a book by its unique identifier.
     *
     * @param id the unique identifier of the book
     * @return the matching {@link Book}
     * @throws LibraryException if the book does not exist
     */
    @Transactional(readOnly = true)
    public Book getBook(@Nonnull UUID id) {
        return bookService.getBookById(id)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.BOOK_NOT_FOUND, "Book not found"));
    }

    /**
     * Retrieves a book by its ISBN.
     *
     * @param isbn the ISBN of the book
     * @return the matching {@link Book}
     * @throws LibraryException if the book does not exist
     */
    @Transactional(readOnly = true)
    public Book getBookByISBN(String isbn) {
        return bookService.getBookByIsbn(isbn)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.BOOK_NOT_FOUND, "Book not found"));
    }

    /**
     * Retrieves books by a partial title match.
     *
     * @param title the title fragment
     * @return list of {@link Book} entities whose title matches
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksByTitle(String title) {
        return bookService.getBookByTitle(title);
    }

    /**
     * Retrieves books by a partial author match.
     *
     * @param author the author name fragment
     * @return list of {@link Book} entities whose author matches
     */
    @Transactional(readOnly = true)
    public List<Book> getBooksByAuthor(String author) {
        return bookService.getBookByAuthor(author);
    }

    /**
     * Adds a new book to the library.
     *
     * @param book the book to create
     * @return the created {@link Book}
     */
    @Transactional
    public Book addBook(@Nonnull Book book) {
        return bookService.createBook(book);
    }

    /**
     * Updates an existing book in the library.
     *
     * @param bookId      the id of the book to update
     * @param updatedBook the updated book details
     * @return the updated {@link Book}
     * @throws LibraryException if the book is not found or total copies < active
     *                          loans
     */
    @Transactional
    public Book updateBook(@Nonnull UUID bookId, @Nonnull Book updatedBook) {
        Book existingBook = bookService.getBookById(bookId)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.BOOK_NOT_FOUND, "Book not found"));

        long activeLoans = loanService.getActiveLoanCountForBook(bookId);
        if (updatedBook.getTotalCopies() < activeLoans) {
            throw new LibraryException(LibraryExceptionType.BOOK_CANNOT_REDUCE_COPIES,
                    String.format("Cannot reduce total copies below active loans (%d)", activeLoans));
        }

        return bookService.updateBook(existingBook, updatedBook, activeLoans);
    }

    /**
     * Removes a book from the library.
     *
     * @param id the unique identifier of the book
     * @throws LibraryException if the book has active loans
     */
    @Transactional
    public void removeBook(@Nonnull UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForBook(id) > 0;
        if (hasLoans) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_BOOK_CANNOT_DELETE_ACTIVE_LOANS,
                    "Cannot delete book while copies are still on loan");
        }
        bookService.deleteBook(id, hasLoans);
    }

    /**
     * Retrieves a paginated list of all books.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Book} entities
     */
    @Transactional(readOnly = true)
    public Page<Book> getBooks(@Nonnull Pageable pageable) {
        return bookService.getBooks(pageable);
    }

    // ----------------------------------------
    // Members
    // ----------------------------------------

    /**
     * Retrieves a member by identifier.
     *
     * @param id the unique identifier of the member
     * @return the matching {@link Member}
     * @throws LibraryException if member not found
     */
    @Transactional(readOnly = true)
    public Member getMember(@Nonnull UUID id) {
        return memberService.getMemberById(id)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.MEMBER_NOT_FOUND, "Member not found"));
    }

    /**
     * Retrieves a member by email.
     *
     * @param email the email of the member
     * @return the matching {@link Member}
     * @throws LibraryException if member not found
     */
    @Transactional(readOnly = true)
    public Member getMemberByEmail(@Nonnull String email) {
        return memberService.getMemberByEmail(email)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.MEMBER_NOT_FOUND, "Member not found"));
    }

    /**
     * Retrieves members by name.
     *
     * @param firstName first name search term
     * @param lastName  last name search term
     * @return list of matching {@link Member} entities
     */
    @Transactional(readOnly = true)
    public List<Member> getMembersByName(@Nonnull String firstName, @Nonnull String lastName) {
        return memberService.getMembersByName(firstName, lastName);
    }

    /**
     * Registers a new member.
     *
     * @param member the member to create
     * @return the created {@link Member}
     */
    public Member registerMember(@Nonnull Member member) {
        return memberService.createMember(member);
    }

    /**
     * Updates an existing member.
     *
     * @param memberId      the id of the member to update
     * @param updatedMember the updated member details
     * @return the updated {@link Member}
     * @throws LibraryException if member not found
     */
    public Member updateMember(@Nonnull UUID memberId, @Nonnull Member updatedMember) {
        Member existingMember = memberService.getMemberById(memberId)
                .orElseThrow(() -> new LibraryException(LibraryExceptionType.MEMBER_NOT_FOUND, "Member not found"));
        return memberService.updateMember(existingMember, updatedMember);
    }

    /**
     * Removes a member from the library.
     *
     * @param id the unique identifier of the member
     * @throws LibraryException if the member has active loans
     */
    @Transactional
    public void removeMember(@Nonnull UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForMember(id) > 0;
        if (hasLoans) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_MEMBER_CANNOT_DELETE_ACTIVE_LOANS,
                    "Cannot delete member while they have active loans");
        }
        memberService.deleteMember(id, hasLoans);
    }

    /**
     * Retrieves a paginated list of all members.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Member} entities
     */
    @Transactional(readOnly = true)
    public Page<Member> getMembers(@Nonnull Pageable pageable) {
        return memberService.getMembers(pageable);
    }

    // ----------------------------------------
    // Loans
    // ----------------------------------------

    /**
     * Retrieves a paginated list of all loans.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoans(@Nonnull Pageable pageable) {
        return loanService.getLoans(pageable);
    }

    /**
     * Retrieves loans for a specific member.
     *
     * @param memberId the UUID of the member
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByMember(@Nonnull UUID memberId, @Nonnull Pageable pageable) {
        return loanService.getLoansByMember(memberId, pageable);
    }

    /**
     * Retrieves loans for a specific book.
     *
     * @param bookId   the UUID of the book
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} entities
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByBook(@Nonnull UUID bookId, @Nonnull Pageable pageable) {
        return loanService.getLoansByBook(bookId, pageable);
    }

    // ----------------------------------------
    // Checkout & Return
    // ----------------------------------------

    /**
     * Checks out a book for a member, enforcing inventory and loan policy rules.
     *
     * @param memberId the UUID of the borrowing member
     * @param bookId   the UUID of the book to checkout
     * @return the created {@link Loan}
     * @throws LibraryException if borrowing rules are violated
     */
    @Transactional
    public Loan checkoutBook(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        Member member = getMember(memberId);
        Book book = getBook(bookId);

        if (book.getAvailableCopies() <= 0) {
            throw new LibraryException(LibraryExceptionType.BOOK_NO_AVAILABLE_COPIES,
                    "There are no available copies for this book");
        }

        LoanStatsProjection stats = loanService.getLoanStatsProjection(memberId, bookId);

        if (stats.getActiveLoans() >= policy.getMaxActive()) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_MEMBER_MAX_ACTIVE_LOANS_EXCEEDED,
                    String.format("This member has reached the maximum number of loans (%d).", policy.getMaxActive()));
        }

        if (stats.getHasThisBook()) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_MEMBER_ALREADY_HAS_BOOK,
                    "This member already has this book checked out");
        }

        if (stats.getOverdueLoans() > policy.getOverdueBlockThreshold()) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_MEMBER_BLOCKED_DUE_TO_OVERDUE,
                    String.format("This member has %d overdue loans and must return %d books before borrowing another.",
                            stats.getOverdueLoans(),
                            stats.getOverdueLoans() - policy.getOverdueBlockThreshold()));
        }

        booksCheckedOut.increment();

        bookService.decrementAvailableCopies(bookId);
        return loanService.createLoanRecord(member, book, policy.getLoanDurationDays());
    }

    /**
     * Returns a borrowed book by closing the active loan and updating inventory.
     *
     * @param memberId the UUID of the member returning the book
     * @param bookId   the UUID of the book being returned
     * @throws LibraryException if no active loan is found
     */
    @Transactional
    public void returnBook(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        long closed = loanService.closeLoanRecord(memberId, bookId);
        if (closed == 0) {
            throw new LibraryException(LibraryExceptionType.LIBRARY_NO_ACTIVE_LOAN,
                    "No active loan found to return");
        }

        booksReturned.increment();

        bookService.incrementAvailableCopies(bookId);
    }

}
