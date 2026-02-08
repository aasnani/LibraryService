package com.library.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRequest(
    @NotBlank String id,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email
) {}
