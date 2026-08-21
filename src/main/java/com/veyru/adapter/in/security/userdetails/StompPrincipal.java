package com.veyru.adapter.in.security.userdetails;

import java.security.Principal;

public class StompPrincipal implements Principal {
  private final String userId;

  public StompPrincipal(String userId) {
    this.userId = userId;
  }

  @Override
  public String getName() {
    return userId;
  }
}
