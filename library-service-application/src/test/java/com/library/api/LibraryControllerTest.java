package com.library.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.LibraryService;
import com.library.api.request.CreateBookRequest;
import com.library.api.request.UpdateBookRequest;
import com.library.api.request.CreateMemberRequest;
import com.library.api.request.UpdateMemberRequest;
import com.library.books.Book;
import com.library.loans.Loan;
import com.library.members.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibraryController.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    private Book sampleBook;
    private Member sampleMember;
    private Loan sampleLoan;

    private UUID bookId;
    private UUID memberId;

    @BeforeEach
    void setup() {
        bookId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        sampleBook = new Book();
        sampleBook.setId(bookId);
        sampleBook.setTitle("Sample Book");
        sampleBook.setAuthor("John Doe");
        sampleBook.setIsbn("123-456");
        sampleBook.setTotalCopies(5);

        sampleMember = new Member();
        sampleMember.setId(memberId);
        sampleMember.setFirstName("Jane");
        sampleMember.setLastName("Doe");
        sampleMember.setEmail("jane.doe@example.com");

        sampleLoan = new Loan();
        sampleLoan.setBook(sampleBook);
        sampleLoan.setMember(sampleMember);
        sampleLoan.setDueDate(LocalDate.now().plusDays(7));
    }

    // -------------------------------
    // BOOK TESTS
    // -------------------------------

    @Test
    void testGetAllBooks() throws Exception {
        when(libraryService.getBooks(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook)));

        mockMvc.perform(get("/api/library/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample Book"));
    }

    @Test
    void testGetBook() throws Exception {
        when(libraryService.getBook(bookId)).thenReturn(sampleBook);

        mockMvc.perform(get("/api/library/books/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("John Doe"));
    }

    @Test
    void testAddBook() throws Exception {
        CreateBookRequest request = new CreateBookRequest("New Book", "Author X", "987-654", 3);
        when(libraryService.addBook(ArgumentMatchers.any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Book"));
    }

    @Test
    void testUpdateBook() throws Exception {
        UpdateBookRequest request = new UpdateBookRequest(bookId.toString(), "Updated Title", "Updated Author", "111-222", 10);
        when(libraryService.updateBook(eq(bookId), ArgumentMatchers.any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(put("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Book"));
    }

    @Test
    void testRemoveBook() throws Exception {
        doNothing().when(libraryService).removeBook(bookId);

        mockMvc.perform(delete("/api/library/books/{bookId}", bookId))
                .andExpect(status().isNoContent());
    }

    // -------------------------------
    // MEMBER TESTS
    // -------------------------------

    @Test
    void testGetAllMembers() throws Exception {
        when(libraryService.getMembers(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleMember)));

        mockMvc.perform(get("/api/library/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Jane"));
    }

    @Test
    void testGetMember() throws Exception {
        when(libraryService.getMember(memberId)).thenReturn(sampleMember);

        mockMvc.perform(get("/api/library/members/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void testRegisterMember() throws Exception {
        CreateMemberRequest request = new CreateMemberRequest("Jane", "Doe", "jane.doe@example.com");
        when(libraryService.registerMember(ArgumentMatchers.any(Member.class))).thenReturn(sampleMember);

        mockMvc.perform(post("/api/library/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testUpdateMember() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest(memberId.toString(), "Jane", "Doe", "jane.doe@example.com");
        when(libraryService.updateMember(eq(memberId), ArgumentMatchers.any(Member.class))).thenReturn(sampleMember);

        mockMvc.perform(put("/api/library/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void testRemoveMember() throws Exception {
        doNothing().when(libraryService).removeMember(memberId);

        mockMvc.perform(delete("/api/library/members/{memberId}", memberId))
                .andExpect(status().isNoContent());
    }

    // -------------------------------
    // LOAN TESTS
    // -------------------------------

    @Test
    void testCheckoutBook() throws Exception {
        when(libraryService.checkoutBook(memberId, bookId)).thenReturn(sampleLoan);

        mockMvc.perform(post("/api/library/loans/checkout")
                        .param("memberId", memberId.toString())
                        .param("bookId", bookId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.title").value("Sample Book"))
                .andExpect(jsonPath("$.member.firstName").value("Jane"));
    }

    @Test
    void testReturnBook() throws Exception {
        doNothing().when(libraryService).returnBook(memberId, bookId);

        mockMvc.perform(post("/api/library/loans/return")
                        .param("memberId", memberId.toString())
                        .param("bookId", bookId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllLoans() throws Exception {
        when(libraryService.getLoans(ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleLoan)));

        mockMvc.perform(get("/api/library/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].book.title").value("Sample Book"));
    }

    @Test
    void testGetLoansByMember() throws Exception {
        when(libraryService.getLoansByMember(eq(memberId), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleLoan)));

        mockMvc.perform(get("/api/library/loans/member/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].member.firstName").value("Jane"));
    }

    @Test
    void testGetLoansByBook() throws Exception {
        when(libraryService.getLoansByBook(eq(bookId), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleLoan)));

        mockMvc.perform(get("/api/library/loans/book/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].book.title").value("Sample Book"));
    }
}
