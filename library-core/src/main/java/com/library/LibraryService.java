package com.library;

import com.library.books.Book;
import com.library.books.BookService;
import com.library.members.Member;
import com.library.members.MemberService;

import jakarta.annotation.Nonnull;

import com.library.loans.Loan;
import com.library.loans.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;

    // --- ORCHESTRATION LOGIC (The "Smart" Stuff) ---

    @Transactional
    public Loan checkoutBook(@Nonnull UUID memberId, @Nonnull UUID bookId) {
        Member member = memberService.getMemberById(memberId);
        Book book = bookService.getBookById(bookId);

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for: " + book.getTitle());
        }

        // Rule: Max 5 books per member
        if (loanService.getActiveLoanCountForMember(memberId) >= 5) {
            throw new IllegalStateException("Member has reached the maximum loan limit (5)");
        }

        // Rule: Cannot borrow the same book twice
        if (loanService.getActiveLoanCountForBookAndMember(bookId, memberId) > 0) {
            throw new IllegalStateException("Member already has this book checked out");
        }

        // Update Book Inventory
        long activeLoans = loanService.getActiveLoanCountForBook(bookId) + 1;
        bookService.updateBook(bookId, book, activeLoans);

        return loanService.createLoanRecord(member, book, 14);
    }

    @Transactional
    public void returnBook(UUID memberId, UUID bookId) {
        loanService.closeLoanRecord(memberId, bookId);

        Book book = bookService.getBookById(bookId);
        long remainingLoans = loanService.getActiveLoanCountForBook(bookId);
        bookService.updateBook(bookId, book, remainingLoans);
    }

    // --- DELEGATED CRUD (The "Standard" Stuff) ---

    // Books
    public List<Book> getAllBooks() { return bookService.getAllActiveBooks(); }
    public Book getBook(UUID id) { return bookService.getBookById(id); }
    public Book addBook(Book book) { return bookService.createBook(book); }

    @Transactional
    public void removeBook(UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForBook(id) > 0;
        bookService.deleteBook(id, hasLoans);
    }

    // Members
    public List<Member> getAllMembers() { return memberService.getAllMembers(); }
    public Member getMember(UUID id) { return memberService.getMemberById(id); }
    public Member registerMember(Member member) { return memberService.createMember(member); }

    @Transactional
    public void removeMember(UUID id) {
        boolean hasLoans = loanService.getActiveLoanCountForMember(id) > 0;
        memberService.deleteMember(id, hasLoans);
    }
}