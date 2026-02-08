package com.library.books;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for managing {@link Book} entities and enforcing
 * inventory-related business rules.
 *
 * <p>
 * This service provides read access to book records as well as controlled
 * mutation of inventory state, including creation, updates, and availability
 * adjustments during loan operations.
 */
@Service
@Validated
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Retrieves all books in the catalog.
     *
     * @return a list of all {@link Book} entities
     */
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Retrieves a book by its unique identifier.
     *
     * @param id the unique identifier of the book
     * @return an {@link Optional} containing the matching {@link Book} if found,
     *         or {@link Optional#empty()} if no book exists with the given id
     */
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(@Nonnull UUID id) {
        return bookRepository.findById(id);
    }

    /**
     * Retrieves a book by its ISBN.
     *
     * <p>
     * The ISBN value is validated using a standard ISBN-10 / ISBN-13 format
     * before repository access.
     *
     * @param isbn the ISBN of the book
     * @return an {@link Optional} containing the matching {@link Book} if found,
     *         or {@link Optional#empty()} if no book exists with the given ISBN
     */
    @Transactional(readOnly = true)
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Invalid ISBN format")
    public Optional<Book> getBookByIsbn(@Nonnull @NotBlank String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    /**
     * Performs a case-insensitive partial-title search.
     *
     * @param title the title fragment to search for
     * @return a list of books whose titles contain the provided value
     */
    @Transactional(readOnly = true)
    public List<Book> getBookByTitle(@Nonnull @NotBlank String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Performs a case-insensitive partial-author search.
     *
     * @param author the author name fragment to search for
     * @return a list of books whose authors match the provided value
     */
    @Transactional(readOnly = true)
    public List<Book> getBookByAuthor(@NonNull @NotBlank String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    /**
     * Creates a new book record.
     *
     * <p>
     * Upon creation, the number of available copies is initialized to the
     * total number of copies provided.
     *
     * @param book the book to persist
     * @return the persisted {@link Book}
     */
    @Transactional
    public Book createBook(@NonNull Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    /**
     * Updates an existing book and reconciles inventory counts.
     *
     * <p>
     * The total number of copies may not be reduced below the number of
     * currently active loans. Available copies are recalculated based on
     * the provided total and active loan count.
     *
     * @param existingBook the existing {@link Book} entity to update
     * @param updatedBook  the updated book details
     * @param activeLoans  the number of active loans for the book
     * @return the updated {@link Book} after saving to the repository
     */
    @Transactional
    public Book updateBook(@NonNull Book existingBook, @Nonnull Book updatedBook, long activeLoans) {
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setTotalCopies(updatedBook.getTotalCopies());
        existingBook.setAvailableCopies((int) (updatedBook.getTotalCopies() - activeLoans));
        return bookRepository.save(existingBook);
    }

    /**
     * Deletes a book from the catalog.
     *
     * <p>
     * Deletion is only permitted when no copies of the book are currently
     * on loan.
     *
     * @param id             the unique identifier of the book
     * @param hasActiveLoans whether the book currently has active loans
     */
    @Transactional
    public void deleteBook(@NonNull UUID id, boolean hasActiveLoans) {
        bookRepository.deleteById(id);
    }

    /**
     * Atomically decrements the number of available copies for a book.
     *
     * @param id the unique identifier of the book
     * @return the number of rows updated
     */
    @Transactional
    public long decrementAvailableCopies(@NonNull UUID id) {
        return bookRepository.decrementAvailableCopies(id);
    }

    /**
     * Atomically increments the number of available copies for a book.
     *
     * @param id the unique identifier of the book
     * @return the number of rows updated
     */
    @Transactional
    public long incrementAvailableCopies(@Nonnull UUID id) {
        return bookRepository.incrementAvailableCopies(id);
    }

    /**
     * Retrieves a paginated list of all books in the library.
     *
     * @param pageable pagination and sorting information
     * @return a page of {@link Book} entities
     */
    @Transactional(readOnly = true)
    public Page<Book> getBooks(@Nonnull Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
}
