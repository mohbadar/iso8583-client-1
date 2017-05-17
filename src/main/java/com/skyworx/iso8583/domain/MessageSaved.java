package com.skyworx.iso8583.domain;

public class MessageSaved {
    private Message message;

    public MessageSaved(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
