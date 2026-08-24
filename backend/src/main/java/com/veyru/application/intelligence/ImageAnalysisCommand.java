package com.veyru.application.intelligence;

public record ImageAnalysisCommand(String imageBase64, String mimeType, String userId) {}
