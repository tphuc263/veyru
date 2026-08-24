package com.veyru.application.port.out;

import com.veyru.application.identity.AuthenticatedUser;

public interface AccessTokenIssuer {
  String issue(AuthenticatedUser user);
}
