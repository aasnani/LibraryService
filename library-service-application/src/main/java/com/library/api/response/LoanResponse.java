package com.library.api.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * API response representation of a {@link com.library.loans.Loan}.
 *
 * <p>Flattens related {@code Book} and {@code Member} references to avoid
 * serialization of lazy-loaded Hibernate proxies.</p>
 */
public record LoanResponse(
        UUID id,

        UUID bookId,
        String bookTitle,

        UUID memberId,
        String memberFirstName,
        String memberLastName,

        OffsetDateTime borrowedAt,
        LocalDate dueDate,
        OffsetDateTime returnedAt,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
