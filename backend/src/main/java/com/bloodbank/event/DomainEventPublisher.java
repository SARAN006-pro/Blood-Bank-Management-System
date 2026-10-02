package com.bloodbank.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
