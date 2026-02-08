package com.library.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateLoanRequest(
    @NotBlank String memberId,
    @NotBlank String bookId
) {}
