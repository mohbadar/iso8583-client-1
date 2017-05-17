package com.skyworx.iso8583.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyworx.iso8583.EventBus;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jpos.iso.ISOMsg;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.skyworx.iso8583.DatabaseUtils.db;

public class Message implements Serializable{
    public static final String MESSAGES_NAME = "iso-messages";
    public static final String HISTORIES_NAME = "iso-message-history";
    public static final HTreeMap<UUID,String> MESSAGES = db.hashMap(MESSAGES_NAME, Serializer.UUID, Serializer.STRING).createOrOpen();
    public static final HTreeMap<UUID,String> HISTORIES = db.hashMap(HISTORIES_NAME, Serializer.UUID, Serializer.STRING).createOrOpen();
    private UUID id;
    private StringProperty name = new SimpleStringProperty();
    private ObservableList<BitMessage> bits = FXCollections.observableArrayList();
    private StringProperty mti = new SimpleStringProperty();
    private static final ObjectMapper om = new ObjectMapper();
    private String historyLabel;

    public Message copy(){
        return deserialize(this.serialize());
    }

    public void createHistory(String historyLabel){
        Message copyOfMessage = copy();
        try {
            HISTORIES.put(UUID.randomUUID(), copyOfMessage.serialize());
            db.commit();
            EventBus.post(new MessageHistoryCreated(this));
        }catch (Exception e){
            db.rollback();
            throw new RuntimeException("Unhandled Exception", e);
        }
    }

    public void save(){
        try {
            if(this.id == null){
                this.id = UUID.randomUUID();
            }
            this.setHistoryLabel(null);
            MESSAGES.put(this.id, this.serialize());
            db.commit();
            EventBus.post(new MessageSaved(this));
        }catch (Exception e){
            db.rollback();
            throw new RuntimeException("Unhandled Exception", e);
        }
    }


    public static void delete(UUID id){
        MESSAGES.remove(id);
        db.commit();
    }

    public static List<Message> findAll(){
        return new ArrayList<>(MESSAGES.getValues().stream().map(Message::deserialize).collect(Collectors.toList()));
    }

    public static Message deserialize(String strMessage){
        Message message = null;
        try {
            message = om.readValue(strMessage, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    public String serialize(){
        try {
            return om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unhandled exception", e);
        }
    }

    public String getHistoryLabel() {
        return historyLabel;
    }

    public void setHistoryLabel(String historyLabel) {
        this.historyLabel = historyLabel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name.get();
    }

    public void addBit(BitMessage message){
        this.bits.add(message);
    }

    public void addBit(int bit, String value){
        this.bits.add(new BitMessage(bit, value));
    }
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public List<BitMessage> getBits() {
        return bits;
    }

    public void setBits(List<BitMessage> bits) {
        this.bits = FXCollections.observableArrayList(bits);
    }

    public ObservableList<BitMessage> bits(){
        return bits;
    }

    public ISOMsg toIso() {
        ISOMsg isoMsg = new ISOMsg(this.getMti());
        bits.forEach(message -> {
            isoMsg.set(message.getBit(), message.getValue());
        });
        return isoMsg;
    }

    public String getMti() {
        return mti.get();
    }

    public StringProperty mtiProperty() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti.set(mti);
    }

    public static class BitMessage implements Serializable{
        private ObjectProperty<Integer> bit = new SimpleObjectProperty<>();
        private StringProperty value = new SimpleStringProperty();

        public BitMessage() {
        }

        public BitMessage(int bit, String value) {
            setBit(bit);
            setValue(value);
        }

        public int getBit() {
            return bit.get();
        }

        public ObjectProperty<Integer> bitProperty() {
            return bit;
        }

        public void setBit(int bit) {
            this.bit.set(bit);
        }

        public String getValue() {
            return value.get();
        }

        public StringProperty valueProperty() {
            return value;
        }

        public void setValue(String value) {
            this.value.set(value);
        }
    }
}
