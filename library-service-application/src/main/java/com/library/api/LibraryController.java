package com.library.api;

import com.library.LibraryService;
import com.library.api.request.CreateBookRequest;
import com.library.api.request.CreateMemberRequest;
import com.library.api.request.UpdateBookRequest;
import com.library.api.request.UpdateMemberRequest;
import com.library.books.Book;
import com.library.loans.Loan;
import com.library.members.Member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller exposing endpoints for core library operations.
 *
 * <p>
 * Handles book management, member management, and loan operations through
 * a unified facade ({@link LibraryService}). Supports pagination for
 * list endpoints and transactional operations for checkout and return of books.
 * </p>
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    // -------------------------------
    // BOOK ENDPOINTS
    // -------------------------------

    /**
     * Retrieves all books in the library with pagination.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Book} records
     */
    @GetMapping("/books")
    public Page<Book> getAllBooks(Pageable pageable) {
        return libraryService.getBooks(pageable);
    }

    /**
     * Retrieves a single book by its identifier.
     *
     * @param bookId the UUID of the book
     * @return the corresponding {@link Book}
     */
    @GetMapping("/books/{bookId}")
    public Book getBook(@PathVariable UUID bookId) {
        return libraryService.getBook(bookId);
    }

    /**
     * Adds a new book to the library.
     *
     * @param request the {@link CreateBookRequest} request containing the book details
     * @return the created {@link Book}
     */
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody @Valid CreateBookRequest request) {
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setTotalCopies(request.totalCopies());
        return libraryService.addBook(book);
    }

    /**
     * Updates an existing book in the library.
     *
     * @param request the {@link UpdateBookRequest} request containing the updated book details
     * @return the updated {@link Book}
     */
    @PutMapping("/books")
    @ResponseStatus(HttpStatus.OK)
    public Book updateBook(@RequestBody @Valid UpdateBookRequest request) {
        Book updatedBook = new Book();
        updatedBook.setTitle(request.title());
        updatedBook.setAuthor(request.author());
        updatedBook.setIsbn(request.isbn());
        updatedBook.setTotalCopies(request.totalCopies());

        return libraryService.updateBook(UUID.fromString(request.id()), updatedBook);
    }

    /**
     * Removes a book from the library.
     *
     * @param bookId the UUID of the book to remove
     */
    @DeleteMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBook(@PathVariable UUID bookId) {
        libraryService.removeBook(bookId);
    }

    // -------------------------------
    // MEMBER ENDPOINTS
    // -------------------------------

    /**
     * Retrieves all members with pagination.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Member} records
     */
    @GetMapping("/members")
    public Page<Member> getAllMembers(Pageable pageable) {
        return libraryService.getMembers(pageable);
    }

    /**
     * Retrieves a member by UUID.
     *
     * @param memberId the UUID of the member
     * @return the corresponding {@link Member}
     */
    @GetMapping("/members/{memberId}")
    public Member getMember(@PathVariable UUID memberId) {
        return libraryService.getMember(memberId);
    }

    /**
     * Registers a new library member.
     *
     * @param member the {@link Member} to create
     * @return the created {@link Member}
     */
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Member registerMember(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        return libraryService.registerMember(member);
    }

    /**
     * Updates an existing member in the library.
     *
     * @param request the {@link UpdateMemberRequest} request containing the updated member details
     * @return the updated {@link Member}
     */
    @PutMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public Member updateMembers(@RequestBody @Valid UpdateMemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        return libraryService.updateMember(UUID.fromString(request.id()), member);
    }


    /**
     * Removes a member from the library.
     *
     * @param memberId the UUID of the member to remove
     */
    @DeleteMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID memberId) {
        libraryService.removeMember(memberId);
    }

    // -------------------------------
    // LOAN ENDPOINTS
    // -------------------------------

    /**
     * Checks out a book for a member.
     *
     * @param memberId the UUID of the member
     * @param bookId   the UUID of the book
     * @return the created {@link Loan}
     */
    @PostMapping("/loans/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public Loan checkoutBook(@RequestParam UUID memberId, @RequestParam UUID bookId) {
        return libraryService.checkoutBook(memberId, bookId);
    }

    /**
     * Returns a borrowed book.
     *
     * @param memberId the UUID of the member
     * @param bookId   the UUID of the book
     */
    @PostMapping("/loans/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@RequestParam UUID memberId, @RequestParam UUID bookId) {
        libraryService.returnBook(memberId, bookId);
    }

    /**
     * Retrieves all loans with pagination.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} records
     */
    @GetMapping("/loans")
    public Page<Loan> getAllLoans(Pageable pageable) {
        return libraryService.getLoans(pageable);
    }

    /**
     * Retrieves all loans for a specific member with pagination.
     *
     * @param memberId the UUID of the member
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} records for the member
     */
    @GetMapping("/loans/member/{memberId}")
    public Page<Loan> getLoansByMember(@PathVariable UUID memberId, Pageable pageable) {
        return libraryService.getLoansByMember(memberId, pageable);
    }

    /**
     * Retrieves all loans for a specific book with pagination.
     *
     * @param bookId   the UUID of the book
     * @param pageable pagination and sorting information
     * @return a page of {@link Loan} records for the book
     */
    @GetMapping("/loans/book/{bookId}")
    public Page<Loan> getLoansByBook(@PathVariable UUID bookId, Pageable pageable) {
        return libraryService.getLoansByBook(bookId, pageable);
    }
}
