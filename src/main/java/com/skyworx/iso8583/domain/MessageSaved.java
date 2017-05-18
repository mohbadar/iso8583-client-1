package com.skyworx.iso8583.domain;

public class MessageSaved {
    private Message message;
    private boolean isNew;


    public MessageSaved(Message message, boolean isNew) {
        this.message = message;
        this.isNew = isNew;
    }

    public Message getMessage() {
        return message;
    }

    public boolean isNew() {
        return isNew;
    }
}
