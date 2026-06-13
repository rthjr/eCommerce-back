package com.ecommerce.correlation.model;

public record RootCauseSuspect(
        String type,
        String target,
        String reason,
        int score
) {
}
