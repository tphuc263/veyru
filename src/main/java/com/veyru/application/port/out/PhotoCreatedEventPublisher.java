package com.veyru.application.port.out;

import com.veyru.application.event.PhotoCreatedEvent;

public interface PhotoCreatedEventPublisher {
  void publish(PhotoCreatedEvent event);
}
