package com.library.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * API response representation of a {@link com.library.members.Member}.
 *
 * <p>Designed to support future role-based field visibility
 * (e.g., hiding email from non-privileged users).</p>
 */
public record MemberResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
