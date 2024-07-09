package com.rp.authorization.events;

import org.springframework.context.ApplicationEvent;

import com.rp.authorization.models.entities.User;

public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private User user;

    public OnRegistrationCompleteEvent(User user) {
        super(user);

        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public OnRegistrationCompleteEvent setUser(User user) {
        this.user = user;
        return this;
    }
}
