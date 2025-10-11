package com.example.jdbcclient.dto;

import java.math.BigDecimal;

public record CustomerSummary(
        Long id, String name, String email, Integer orderCount, BigDecimal totalSpent) {}
