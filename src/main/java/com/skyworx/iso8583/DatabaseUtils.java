package com.skyworx.iso8583;

import com.skyworx.iso8583.domain.Message;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import static com.skyworx.iso8583.repository.MessageRepository.createMessage;

public class DatabaseUtils {
    static {
        db = DBMaker
                .fileDB(".iso-clientapp.mapdb")
                .checksumHeaderBypass()
                .transactionEnable()
                .make();
    }

    public static DB db;

    public static void main(String[] args) {
        Message.findAll().forEach(message -> {
            System.out.println(message.getId());
        });
        Message message = createMessage("Sample 2", new Message.BitMessage(2, "99999992"), new Message.BitMessage(7, "101011"));
        message.save();
    }
}
