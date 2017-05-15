package com.skyworx.iso8583.repository;

import com.skyworx.iso8583.domain.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageRepository {

    private static final MessageRepository MESSAGE_REPOSITORY = new MessageRepository();

    public static MessageRepository getInstance() {
        return MESSAGE_REPOSITORY;
    }

    public ObservableList<Message> findMessages(){
        ObservableList<Message> observableList = FXCollections.observableArrayList();
        observableList.add(createMessage("Sample 1", new Message.BitMessage(2, "99999999"), new Message.BitMessage(7, "101010")));
        observableList.add(createMessage("Sample 2", new Message.BitMessage(2, "99999992"),new Message.BitMessage(7, "101011")));
        observableList.add(createMessage("Sample 3", new Message.BitMessage(2, "99999994"),new Message.BitMessage(7, "101013")));
        return observableList;
    }

    public static Message createMessage(String name, Message.BitMessage ...bitMessages){
        Message message = new Message();
        message.setName(name);
        message.setBits(Arrays.asList(bitMessages));
        return message;
    }
}
