package com.library.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Valid
public record PaginationRequest(
    @Min(0) int page,
    @Min(1) @Max(100) int size
) {}