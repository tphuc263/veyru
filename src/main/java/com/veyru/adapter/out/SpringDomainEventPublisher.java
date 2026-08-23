package com.veyru.adapter.out;

import com.veyru.application.port.out.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
  private final ApplicationEventPublisher publisher;

  public void publish(Object event) {
    publisher.publishEvent(event);
  }

  public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }
}
