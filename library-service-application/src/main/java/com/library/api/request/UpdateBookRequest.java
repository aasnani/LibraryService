package com.library.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating a book.
 *
 * Only fields that are allowed to change.
 */
public record UpdateBookRequest(
    @NotBlank String id,
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String isbn,
    @NotNull @Min(0) Integer totalCopies
) {}
