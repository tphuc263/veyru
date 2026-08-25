package com.veyru.application.discovery;

public record GraphAffinity(
    String authorId, boolean followed, long mutualCount, long interactionCount) {}
