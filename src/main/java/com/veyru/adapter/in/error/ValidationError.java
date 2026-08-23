package com.veyru.adapter.in.error;

public record ValidationError(String field, String code, String detail) {}
