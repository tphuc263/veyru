package com.veyru.application.port.out;

import com.veyru.application.identity.LoginResult;

public interface AuthenticationGateway {
  LoginResult authenticate(String identifier, String password);
}
