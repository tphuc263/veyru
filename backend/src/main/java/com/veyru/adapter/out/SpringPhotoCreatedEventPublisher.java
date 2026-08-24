package com.veyru.adapter.out;

import com.veyru.application.event.PhotoCreatedEvent;
import com.veyru.application.port.out.PhotoCreatedEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringPhotoCreatedEventPublisher implements PhotoCreatedEventPublisher {
  private final ApplicationEventPublisher publisher;

  public SpringPhotoCreatedEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void publish(PhotoCreatedEvent event) {
    publisher.publishEvent(event);
  }
}
