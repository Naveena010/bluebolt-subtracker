package com.subtracker.dto.response;

public record ServiceCancelCount(
        String serviceName,
        Long cancelCount
) {}