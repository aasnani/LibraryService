package com.library.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.exceptions.LibraryException;
import com.library.exceptions.LibraryException.LibraryExceptionType;
import com.library.members.MemberRepository;
import com.library.auth.MemberSecurity;
import com.library.auth.InMemoryUserDetailsService;
import com.library.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = LibraryController.class)
@Import({ SecurityConfig.class, MemberSecurity.class })
@WithMockUser(username = "admin@library.com", roles = { "ADMIN" })
class LibraryExceptionHandlerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private com.library.LibraryService libraryService;

        @MockitoBean
        private InMemoryUserDetailsService userDetailsService;

        @MockitoBean(name = "memberSecurity")
        private MemberSecurity memberSecurity;

        @MockitoBean
        private MemberRepository memberRepository;

        @BeforeEach
        void setup() {
                when(memberSecurity.isOwner(any(), anyString())).thenReturn(true);
        }

        // -------------------------------
        // LibraryException test
        // -------------------------------
        @Test
        void whenLibraryExceptionThrown_thenReturnsSafeErrorResponse() throws Exception {
                UUID bookId = UUID.randomUUID();

                when(libraryService.getBook(bookId))
                                .thenThrow(new LibraryException(LibraryExceptionType.BOOK_NOT_FOUND,
                                                "Internal message"));

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

                String payload = objectMapper.writeValueAsString(
                                new com.library.api.request.CreateBookRequest(null, "Author", "123-456", 1));

                mockMvc.perform(post("/api/library/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.path").exists())
                                .andExpect(jsonPath("$.timestamp").exists());
        }
}
