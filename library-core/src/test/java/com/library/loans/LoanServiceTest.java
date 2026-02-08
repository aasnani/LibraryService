package com.library.loans;

import com.library.books.Book;
import com.library.members.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("Should close active loan record and return update count")
    void closeLoanRecord_Success() {
        when(loanRepository.closeLoanRecord(member.getId(), book.getId()))
                .thenReturn(1);

        long updated = loanService.closeLoanRecord(member.getId(), book.getId());

        assertThat(updated).isEqualTo(1L);
        verify(loanRepository).closeLoanRecord(member.getId(), book.getId());
    }

    @Test
    @DisplayName("Should return zero when no active loan exists to close")
    void closeLoanRecord_NoActiveLoan() {
        when(loanRepository.closeLoanRecord(member.getId(), book.getId()))
                .thenReturn(0);

        long updated = loanService.closeLoanRecord(member.getId(), book.getId());

        assertThat(updated).isZero();
        verify(loanRepository).closeLoanRecord(member.getId(), book.getId());
    }

    @Test
    @DisplayName("Should count active loans for a member")
    void getActiveLoanCountForMember_Success() {
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(member.getId())).thenReturn(3L);

        long count = loanService.getActiveLoanCountForMember(member.getId());

        assertThat(count).isEqualTo(3L);
        verify(loanRepository).countByMemberIdAndReturnedAtIsNull(member.getId());
    }

    @Test
    @DisplayName("Should count active loans for a book")
    void getActiveLoanCountForBook_Success() {
        when(loanRepository.countByBookIdAndReturnedAtIsNull(book.getId())).thenReturn(2L);

        long count = loanService.getActiveLoanCountForBook(book.getId());

        assertThat(count).isEqualTo(2L);
        verify(loanRepository).countByBookIdAndReturnedAtIsNull(book.getId());
    }

    @Test
    @DisplayName("Should retrieve loan stats projection")
    void getLoanStatsProjection_Success() {
        LoanStatsProjection projection = mock(LoanStatsProjection.class);
        when(loanRepository.getLoanStatsForMember(member.getId(), book.getId()))
                .thenReturn(projection);

        LoanStatsProjection result = loanService.getLoanStatsProjection(member.getId(), book.getId());

        assertThat(result).isEqualTo(projection);
        verify(loanRepository).getLoanStatsForMember(member.getId(), book.getId());
    }

}
