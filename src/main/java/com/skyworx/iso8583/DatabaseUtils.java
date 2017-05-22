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
}
