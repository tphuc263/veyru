package com.veyru.domain.exception;

public record ValidationError(String field, String code, String detail) {}
