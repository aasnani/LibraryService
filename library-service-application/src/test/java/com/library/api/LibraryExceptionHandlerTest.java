package com.library.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.exceptions.LibraryException;
import com.library.exceptions.LibraryException.LibraryExceptionType;
import com.library.api.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LibraryController.class)
class LibraryExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private com.library.LibraryService libraryService;

    // -------------------------------
    // LibraryException test
    // -------------------------------
    @Test
    void whenLibraryExceptionThrown_thenReturnsSafeErrorResponse() throws Exception {
        UUID bookId = UUID.randomUUID();

        when(libraryService.getBook(bookId))
                .thenThrow(new LibraryException(LibraryExceptionType.BOOK_NOT_FOUND, "Internal message"));

        mockMvc.perform(get("/api/library/books/{bookId}", bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Internal message"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // -------------------------------
    // Validation error test
    // -------------------------------
    @Test
    void whenValidationFails_thenReturnsFieldErrors() throws Exception {
        // Send a CreateBookRequest with missing title (null)
        String payload = objectMapper.writeValueAsString(
                new com.library.api.request.CreateBookRequest(null, "Author", "123-456", 1)
        );

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
