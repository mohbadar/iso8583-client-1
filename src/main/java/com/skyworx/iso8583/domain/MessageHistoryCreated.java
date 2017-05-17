package com.skyworx.iso8583.domain;

public class MessageHistoryCreated {
    private Message message;

    public MessageHistoryCreated(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
