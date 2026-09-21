package com.subtracker.dto.request;

import jakarta.validation.constraints.NotNull;

public record UsageUpdateRequest(
        @NotNull(message = "usedRecently flag is required") Boolean usedRecently
) {}