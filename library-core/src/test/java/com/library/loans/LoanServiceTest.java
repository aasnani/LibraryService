package com.library.loans;

import com.library.books.Book;
import com.library.members.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(UUID.randomUUID());

        book = new Book();
        book.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create loan record with correct due date and timestamps")
    void createLoanRecord_Success() {
        int daysUntilDue = 14;
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        Loan result = loanService.createLoanRecord(member, book, daysUntilDue);

        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getBorrowedAt()).isNotNull();
        assertThat(result.getReturnedAt()).isNull();
        assertThat(result.getDueDate()).isEqualTo(LocalDate.now().plusDays(daysUntilDue));

        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should close loan record by setting returnedAt timestamp")
    void closeLoanRecord_Success() {
        Loan activeLoan = new Loan();
        activeLoan.setBorrowedAt(java.time.OffsetDateTime.now().minusDays(1));
        activeLoan.setReturnedAt(null);

        when(loanRepository.findByMemberIdAndBookIdAndReturnedAtIsNull(member.getId(), book.getId()))
                .thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        loanService.closeLoanRecord(member.getId(), book.getId());

        assertThat(activeLoan.getReturnedAt()).isNotNull();
        verify(loanRepository).save(activeLoan);
    }

    @Test
    @DisplayName("Should throw exception when closing loan that does not exist")
    void closeLoanRecord_NotFound() {
        when(loanRepository.findByMemberIdAndBookIdAndReturnedAtIsNull(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.closeLoanRecord(member.getId(), book.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active loan found");

        verify(loanRepository, never()).save(any());
    }
}