package com.veyru.adapter.in.dto.response.search;

import com.veyru.application.result.search.UserSearchSimpleResult;

public record UserSearchResponseSimple(String id, String username, String imageUrl) {
  public static UserSearchResponseSimple from(UserSearchSimpleResult value) {
    return new UserSearchResponseSimple(value.getId(), value.getUsername(), value.getImageUrl());
  }
}
