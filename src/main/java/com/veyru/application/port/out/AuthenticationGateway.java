package com.veyru.application.port.out;

import com.veyru.application.identity.AuthenticatedUser;

public interface AuthenticationGateway {
  AuthenticatedUser authenticate(String identifier, String password);
}
