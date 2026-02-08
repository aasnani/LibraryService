package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.config.LoanPolicyProperties;
import com.library.loans.LoanService;
import com.library.loans.LoanStatsProjection;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock private BookService bookService;
    @Mock private MemberService memberService;
    @Mock private LoanService loanService;
    @Mock private LoanPolicyProperties policy;

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
        book.setIsbn("ISBN-123");
        book.setAvailableCopies(1);
    }

    @Test
    @DisplayName("Should checkout successfully when all rules are met")
    void checkoutBook_Success() {
        LoanStatsProjection stats = mock(LoanStatsProjection.class);

        when(memberService.getMemberById(memberId)).thenReturn(member);
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(loanService.getLoanStatsProjection(memberId, bookId)).thenReturn(stats);

        when(stats.getActiveLoans()).thenReturn(2L);
        when(stats.getOverdueLoans()).thenReturn(0L);
        when(stats.getHasThisBook()).thenReturn(false);

        when(policy.getMaxActive()).thenReturn(5);
        when(policy.getOverdueBlockThreshold()).thenReturn(0);
        when(policy.getLoanDurationDays()).thenReturn(14);

        when(bookService.decrementAvailableCopies(bookId)).thenReturn(1L);

        libraryService.checkoutBook(memberId, bookId);

        verify(bookService).decrementAvailableCopies(bookId);
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

        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("Should throw error when member exceeds max active loans")
    void checkoutBook_MaxLoansReached() {
        LoanStatsProjection stats = mock(LoanStatsProjection.class);

        when(memberService.getMemberById(memberId)).thenReturn(member);
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(loanService.getLoanStatsProjection(memberId, bookId)).thenReturn(stats);

        when(stats.getActiveLoans()).thenReturn(5L);
        when(policy.getMaxActive()).thenReturn(5);

        assertThatThrownBy(() -> libraryService.checkoutBook(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum loan limit");
    }

    @Test
    @DisplayName("Should return book successfully")
    void returnBook_Success() {
        when(loanService.closeLoanRecord(memberId, bookId)).thenReturn(1);

        libraryService.returnBook(memberId, bookId);

        verify(loanService).closeLoanRecord(memberId, bookId);
        verify(bookService).incrementAvailableCopies(bookId);
    }

    @Test
    @DisplayName("Should throw error when returning non-existent loan")
    void returnBook_NoActiveLoan() {
        when(loanService.closeLoanRecord(memberId, bookId)).thenReturn(0);

        assertThatThrownBy(() -> libraryService.returnBook(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active loan found");

        verify(bookService, never()).incrementAvailableCopies(any());
    }

    @Test
    @DisplayName("Should check for active loans before removing book")
    void removeBook_WithActiveLoans() {
        when(loanService.getActiveLoanCountForBook(bookId)).thenReturn(1L);

        libraryService.removeBook(bookId);

        verify(bookService).deleteBook(bookId, true);
    }
}
