package com.library.books;

import com.library.common.BaseRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Should find books by partial title (ignore case)")
    void shouldFindByTitle() {
        saveBook("Spring Boot in Action", "Craig Walls", "9781617292545", 5001L);

        List<Book> results = bookRepository.findByTitleContainingIgnoreCase("boot");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo("Craig Walls");
    }

    @Test
    @DisplayName("Should find books by partial author (ignore case)")
    void shouldFindByAuthor() {
        saveBook("Java Concurrency", "Brian Goetz", "9780321349606", 5002L);

        List<Book> results = bookRepository.findByAuthorContainingIgnoreCase("goetz");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Concurrency");
    }

    @Test
    @DisplayName("Should fail when ISBN is already taken")
    void shouldFailOnDuplicateIsbn() {
        saveBook("First Book", "Author", "ISBN-DUPE", 6001L);

        assertThrows(DataIntegrityViolationException.class, () -> {
            saveBook("Second Book", "Author", "ISBN-DUPE", 6002L);
        });
    }

    @Test
    @DisplayName("Should fail when Book Number is already taken")
    void shouldFailOnDuplicateBookNumber() {
        saveBook("First Book", "Author", "ISBN-1", 7000L);

        assertThrows(DataIntegrityViolationException.class, () -> {
             saveBook("Second Book", "Author", "ISBN-2", 7000L);
        });
    }

    private void saveBook(String title, String author, String isbn, Long bookNumber) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setBookNumber(bookNumber);
        bookRepository.saveAndFlush(book);
    }
}