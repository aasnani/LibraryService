package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.config.LoanPolicyProperties;
import com.library.loans.LoanService;
import com.library.loans.LoanStatsProjection;
import com.library.members.Member;
import com.library.members.MemberService;
import com.library.exceptions.LibraryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

        when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));
        when(loanService.getLoanStatsProjection(memberId, bookId)).thenReturn(stats);

        when(stats.getActiveLoans()).thenReturn(2L);
        when(stats.getOverdueLoans()).thenReturn(0L);
        when(stats.getHasThisBook()).thenReturn(false);

        when(policy.getMaxActive()).thenReturn(5);
        when(policy.getOverdueBlockThreshold()).thenReturn(0);
        when(policy.getLoanDurationDays()).thenReturn(14);

        libraryService.checkoutBook(memberId, bookId);

        verify(bookService).decrementAvailableCopies(bookId);
        verify(loanService).createLoanRecord(member, book, 14);
    }

    @Test
    @DisplayName("Should throw error if book is out of stock")
    void checkoutBook_OutOfStock() {
        book.setAvailableCopies(0);

        when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> libraryService.checkoutBook(memberId, bookId))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("no available copies");

        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("Should throw error when member exceeds max active loans")
    void checkoutBook_MaxLoansReached() {
        LoanStatsProjection stats = mock(LoanStatsProjection.class);

        // Stub the underlying services, used by LibraryService.getMember() / getBook()
        when(memberService.getMemberById(memberId)).thenReturn(Optional.of(member));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));
        when(loanService.getLoanStatsProjection(memberId, bookId)).thenReturn(stats);

        // Stats values to trigger the exception
        when(stats.getActiveLoans()).thenReturn(5L);
        when(policy.getMaxActive()).thenReturn(5);

        assertThatThrownBy(() -> libraryService.checkoutBook(memberId, bookId))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("maximum number of loans");

        // verify only relevant interactions
        verify(loanService).getLoanStatsProjection(memberId, bookId);
        verify(stats).getActiveLoans();
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
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("No active loan");

        verify(bookService, never()).incrementAvailableCopies(any());
    }

    @Test
    @DisplayName("Should throw error when trying to remove a book with active loans")
    void removeBook_WithActiveLoans() {
        when(loanService.getActiveLoanCountForBook(bookId)).thenReturn(1L);

        assertThatThrownBy(() -> libraryService.removeBook(bookId))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("Cannot delete book while copies are still on loan");

        verify(bookService, never()).deleteBook(any(), anyBoolean());
    }
}
