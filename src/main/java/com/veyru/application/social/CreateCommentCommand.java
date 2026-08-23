package com.veyru.application.social;

import java.util.List;

public record CreateCommentCommand(
    String text, String parentCommentId, List<String> mentionedUsernames) {}
