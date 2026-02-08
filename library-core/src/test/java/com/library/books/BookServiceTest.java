package com.library.books;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();

        sampleBook = new Book();
        sampleBook.setId(bookId);
        sampleBook.setTitle("The Great Gatsby");
        sampleBook.setAuthor("F. Scott Fitzgerald");
        sampleBook.setIsbn("9780743273565");
        sampleBook.setTotalCopies(10);
        sampleBook.setAvailableCopies(10);
    }

    @Test
    void createBook_ShouldInitializeAvailableCopies() {
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book savedBook = bookService.createBook(sampleBook);

        assertThat(savedBook.getAvailableCopies()).isEqualTo(10);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_ShouldUpdateStockAndAvailableCopies() {
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book details = new Book();
        details.setTitle("Updated Title");
        details.setAuthor("Updated Author");
        details.setIsbn("9780132350884");
        details.setTotalCopies(15);

        long activeLoans = 5;

        Book result = bookService.updateBook(sampleBook, details, activeLoans);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getAuthor()).isEqualTo("Updated Author");
        assertThat(result.getIsbn()).isEqualTo("9780132350884");
        assertThat(result.getTotalCopies()).isEqualTo(15);
        assertThat(result.getAvailableCopies()).isEqualTo(10); // 15 - 5

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void deleteBook_ShouldDelete_WhenNoActiveLoans() {
        bookService.deleteBook(bookId, false);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void decrementAvailableCopies_ShouldCallRepository() {
        when(bookRepository.decrementAvailableCopies(bookId)).thenReturn(1);

        long result = bookService.decrementAvailableCopies(bookId);

        assertThat(result).isEqualTo(1L);
        verify(bookRepository).decrementAvailableCopies(bookId);
    }

    @Test
    void incrementAvailableCopies_ShouldCallRepository() {
        when(bookRepository.incrementAvailableCopies(bookId)).thenReturn(1);

        long result = bookService.incrementAvailableCopies(bookId);

        assertThat(result).isEqualTo(1L);
        verify(bookRepository).incrementAvailableCopies(bookId);
    }
}
