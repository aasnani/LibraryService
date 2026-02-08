package com.library.api;

import com.library.LibraryService;
import com.library.api.request.CreateBookRequest;
import com.library.api.request.CreateMemberRequest;
import com.library.api.request.PaginationRequest;
import com.library.api.request.UpdateBookRequest;
import com.library.api.request.UpdateMemberRequest;
import com.library.api.response.BookResponse;
import com.library.api.response.LoanResponse;
import com.library.api.response.MemberResponse;
import com.library.api.request.CreateLoanRequest;
import com.library.books.Book;
import com.library.loans.Loan;
import com.library.members.Member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;

/**
 * REST controller exposing endpoints for core library operations.
 *
 * <p>
 * Handles book management, member management, and loan operations through
 * a unified service ({@link LibraryService}). Supports pagination for
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
     * @return a page of {@link BookResponse} records
     */
    @GetMapping("/books")
    @Operation(summary = "Get all books", tags = "Books")
    public Page<BookResponse> getAllBooks(PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return libraryService.getBooks(pageable).map(this::toBookResponse);
    }

    /**
     * Retrieves a single book by its identifier.
     *
     * @param bookId the UUID of the book
     * @return the corresponding {@link BookResponse}
     */
    @GetMapping("/books/{bookId}")
    @Operation(summary = "Get a book by ID", tags = "Books")
    public BookResponse getBook(@PathVariable UUID bookId) {
        return toBookResponse(libraryService.getBook(bookId));
    }

    /**
     * Adds a new book to the library.
     *
     * @param request the {@link CreateBookRequest} containing the book details
     * @return the created {@link BookResponse}
     */
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new book", tags = "Books")
    public BookResponse addBook(@RequestBody @Valid CreateBookRequest request) {
        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setTotalCopies(request.totalCopies());
        return toBookResponse(libraryService.addBook(book));
    }

    /**
     * Updates an existing book in the library.
     *
     * @param request the {@link UpdateBookRequest} containing the updated book details
     * @return the updated {@link BookResponse}
     */
    @PutMapping("/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a book", tags = "Books")
    public BookResponse updateBook(@RequestBody @Valid UpdateBookRequest request) {
        Book updatedBook = new Book();
        updatedBook.setTitle(request.title());
        updatedBook.setAuthor(request.author());
        updatedBook.setIsbn(request.isbn());
        updatedBook.setTotalCopies(request.totalCopies());
        return toBookResponse(
                libraryService.updateBook(UUID.fromString(request.id()), updatedBook)
        );
    }

    /**
     * Removes a book from the library.
     *
     * @param bookId the UUID of the book to remove
     */
    @DeleteMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a book", tags = "Books")
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
     * @return a page of {@link MemberResponse} records
     */
    @GetMapping("/members")
    @Operation(summary = "Get all members", tags = "Member")
    public Page<MemberResponse> getAllMembers(PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return libraryService.getMembers(pageable).map(this::toMemberResponse);
    }

    /**
     * Retrieves a member by UUID.
     *
     * @param memberId the UUID of the member
     * @return the corresponding {@link MemberResponse}
     */
    @GetMapping("/members/{memberId}")
    @Operation(summary = "Get a member by ID", tags = "Member")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        return toMemberResponse(libraryService.getMember(memberId));
    }

    /**
     * Registers a new library member.
     *
     * @param request the {@link CreateMemberRequest} containing the member details
     * @return the created {@link MemberResponse}
     */
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new member", tags = "Member")
    public MemberResponse registerMember(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        return toMemberResponse(libraryService.registerMember(member));
    }

    /**
     * Updates an existing member in the library.
     *
     * @param request the {@link UpdateMemberRequest} containing updated member details
     * @return the updated {@link MemberResponse}
     */
    @PutMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a member", tags = "Member")
    public MemberResponse updateMembers(@RequestBody @Valid UpdateMemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        return toMemberResponse(
                libraryService.updateMember(UUID.fromString(request.id()), member)
        );
    }

    /**
     * Removes a member from the library.
     *
     * @param memberId the UUID of the member to remove
     */
    @DeleteMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a member", tags = "Member")
    public void removeMember(@PathVariable UUID memberId) {
        libraryService.removeMember(memberId);
    }

    // -------------------------------
    // LOAN ENDPOINTS
    // -------------------------------

    /**
     * Creates a new loan (checks out a book for a member).
     *
     * @param request contains the memberId and bookId
     * @return the created {@link LoanResponse}
     */
    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout a book for a member", tags = "Library")
    public LoanResponse createLoan(@RequestBody @Valid CreateLoanRequest request) {
        return toLoanResponse(
                libraryService.checkoutBook(
                        UUID.fromString(request.memberId()),
                        UUID.fromString(request.bookId())
                )
        );
    }

    /**
     * Marks a loan as returned based on member and book.
     *
     * @param memberId the UUID of the member returning the book
     * @param bookId   the UUID of the book being returned
     */
    @PatchMapping("/loans")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Return a borrowed book", tags = "Library")
    public void returnLoan(@RequestParam UUID memberId, @RequestParam UUID bookId) {
        libraryService.returnBook(memberId, bookId);
    }

    /**
     * Retrieves all loans with pagination.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link LoanResponse} records
     */
    @GetMapping("/loans")
    @Operation(summary = "Get all loans", tags = "Loans")
    public Page<LoanResponse> getAllLoans(PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return libraryService.getLoans(pageable).map(this::toLoanResponse);
    }

    /**
     * Retrieves all loans for a specific member with pagination.
     *
     * @param memberId the UUID of the member
     * @param pageable pagination and sorting information
     * @return a page of {@link LoanResponse} records for the member
     */
    @GetMapping("/loans/member/{memberId}")
    @Operation(summary = "Get loans by member", tags = "Loans")
    public Page<LoanResponse> getLoansByMember(@PathVariable UUID memberId, PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return libraryService.getLoansByMember(memberId, pageable).map(this::toLoanResponse);
    }

    /**
     * Retrieves all loans for a specific book with pagination.
     *
     * @param bookId   the UUID of the book
     * @param pageable pagination and sorting information
     * @return a page of {@link LoanResponse} records for the book
     */
    @GetMapping("/loans/book/{bookId}")
    @Operation(summary = "Get loans by book", tags = "Loans")
    public Page<LoanResponse> getLoansByBook(@PathVariable UUID bookId, PaginationRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        return libraryService.getLoansByBook(bookId, pageable).map(this::toLoanResponse);
    }

    /**
     * Maps a {@link Loan} entity into a {@link LoanResponse} DTO suitable for API output.
     *
     * @param loan the loan entity
     * @return mapped {@link LoanResponse}, or null if input is null
     */
    private LoanResponse toLoanResponse(Loan loan) {
        if (loan == null) {
            return null;
        }
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getMember().getId(),
                loan.getMember().getFirstName(),
                loan.getMember().getLastName(),
                loan.getBorrowedAt(),
                loan.getDueDate(),
                loan.getReturnedAt(),
                loan.getCreatedAt(),
                loan.getUpdatedAt());
    }

    /**
     * Maps a {@link Book} entity into a {@link BookResponse} DTO suitable for API output.
     *
     * @param book the book entity
     * @return mapped {@link BookResponse}, or null if input is null
     */
    private BookResponse toBookResponse(Book book) {
        if (book == null) {
            return null;
        }
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getCreatedAt(),
                book.getUpdatedAt());
    }

    /**
     * Maps a {@link Member} entity into a {@link MemberResponse} DTO suitable for API output.
     *
     * @param member the member entity
     * @return mapped {@link MemberResponse}, or null if input is null
     */
    private MemberResponse toMemberResponse(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberResponse(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getCreatedAt(),
                member.getUpdatedAt());
    }
}
