package com.library.books;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<Book> getAllActiveBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book getBookById(@Nonnull UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Book not found or has been deleted"));
    }

    @Transactional(readOnly = true)
    @Pattern(
        regexp = "^(97(8|9))?\\d{9}(\\d|X)$",
        message = "Invalid ISBN format"
    )
    public Book getBookByIsbn(@Nonnull @NotBlank String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalStateException("Book not found or has been deleted"));
    }

    @Transactional(readOnly = true)
    public Book getBookByBookNumber(long bookNumber) {
        return bookRepository.findByBookNumber(bookNumber)
                .orElseThrow(() -> new IllegalStateException("Book not found or has been deleted"));
    }

    @Transactional(readOnly = true)
    public List<Book> getBookByTitle(@Nonnull @NotBlank String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional(readOnly = true)
    public List<Book> getBookByAuthor(@NonNull @NotBlank String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Transactional
    public Book createBook(@NonNull Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(@NonNull UUID id, Book details, long activeLoans) {
        Book existing = getBookById(id);

        if (details.getTotalCopies() < activeLoans) {
            throw new IllegalStateException(
                    "Cannot reduce total copies below actual active loans: " + activeLoans);
        }

        existing.setTitle(details.getTitle());
        existing.setAuthor(details.getAuthor());
        existing.setIsbn(details.getIsbn());
        existing.setTotalCopies(details.getTotalCopies());

        existing.setAvailableCopies((int) (details.getTotalCopies() - activeLoans));

        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(@NonNull UUID id, boolean hasActiveLoans) {
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete book while copies are still out on loan");
        }

        bookRepository.deleteById(id);
    }
}