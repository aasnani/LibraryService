package com.library.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * API response representation of a {@link com.library.books.Book}.
 *
 * <p>Exposes inventory and auditing information while decoupling the
 * external contract from the persistence entity.</p>
 */
public record BookResponse(
        UUID id,
        String title,
        String author,
        String isbn,
        int totalCopies,
        int availableCopies,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

