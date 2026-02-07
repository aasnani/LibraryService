package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.loans.Loan;
import com.library.loans.LoanService;
import com.library.members.Member;
import com.library.members.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock private BookService bookService;
    @Mock private MemberService memberService;
    @Mock private LoanService loanService;

    @InjectMocks
    private LibraryService libraryService;

    private UUID memberId;
    private UUID bookId;
    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        member = new Member();
        member.setId(memberId);

        book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setAvailableCopies(1);
    }

    @Test
    @DisplayName("Should checkout successfully when all rules are met")
    void checkoutBook_Success() {
        when(memberService.getMemberById(memberId)).thenReturn(member);
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(loanService.getActiveLoanCountForMember(memberId)).thenReturn(2L);
        when(loanService.getActiveLoanCountForBookAndMember(bookId, memberId)).thenReturn(0L);
        when(loanService.getActiveLoanCountForBook(bookId)).thenReturn(5L);

        libraryService.checkoutBook(memberId, bookId);

        verify(bookService).updateBook(eq(bookId), eq(book), eq(6L));
        verify(loanService).createLoanRecord(member, book, 14);
    }

    @Test
    @DisplayName("Should throw error if book is out of stock")
    void checkoutBook_OutOfStock() {
        book.setAvailableCopies(0);
        when(memberService.getMemberById(memberId)).thenReturn(member);
        when(bookService.getBookById(bookId)).thenReturn(book);

        assertThatThrownBy(() -> libraryService.checkoutBook(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No copies available");

        verify(loanService, never()).createLoanRecord(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should throw error if member has 5 or more books")
    void checkoutBook_MaxLoansReached() {
        when(memberService.getMemberById(memberId)).thenReturn(member);
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(loanService.getActiveLoanCountForMember(memberId)).thenReturn(5L);

        assertThatThrownBy(() -> libraryService.checkoutBook(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum loan limit");
    }

    @Test
    @DisplayName("Should update inventory correctly on return")
    void returnBook_Success() {
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(loanService.getActiveLoanCountForBook(bookId)).thenReturn(3L);

        libraryService.returnBook(memberId, bookId);

        verify(loanService).closeLoanRecord(memberId, bookId);
        verify(bookService).updateBook(eq(bookId), eq(book), eq(3L));
    }

    @Test
    @DisplayName("Should check for active loans before removing a book")
    void removeBook_LogicCheck() {
        when(loanService.getActiveLoanCountForBook(bookId)).thenReturn(1L);

        libraryService.removeBook(bookId);

        verify(bookService).deleteBook(bookId, true);
    }
}