package com.example.jdbcclient.dto;

import com.example.jdbcclient.model.UserStatus;

public record UserFilter(String email, UserStatus status, Integer minAge) {}
