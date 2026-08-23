package com.veyru.application.port.out;

public interface DomainEventPublisher {
  void publish(Object event);
}
