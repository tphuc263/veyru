package com.veyru.exceptions;

public record ValidationError(String field, String code, String detail) {}
