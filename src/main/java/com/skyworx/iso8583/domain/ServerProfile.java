package com.skyworx.iso8583.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.channel.BCDChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.packager.*;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.skyworx.iso8583.DatabaseUtils.db;

public class ServerProfile {
    public static final String COLLECTION_NAME = "server-profile";
    public static final HTreeMap<UUID,String> COLLECTION = db.hashMap(COLLECTION_NAME, Serializer.UUID, Serializer.STRING).createOrOpen();
    private final static ObjectMapper om = new ObjectMapper();
    public enum Packager{
        GENERIC,
        ISO_1993_ASCII,
        ISO_1993_BINNARY,
        ISO_1987_ASCII,
        ISO_1987_BINNARY,
        ISO_1987_BITMAP_B}


    public enum Channel{ASCII,BCD,NAC}

    private UUID id;
    private StringProperty name = new SimpleStringProperty();
    private StringProperty host = new SimpleStringProperty();
    private ObjectProperty<Integer> port = new SimpleObjectProperty<>();
    private ObjectProperty<Packager> packager = new SimpleObjectProperty<>();
    private ObjectProperty<Channel> channel = new SimpleObjectProperty<>();
    private StringProperty genericFileName = new SimpleStringProperty();
    private StringProperty channelHeader = new SimpleStringProperty();

    public void update(ServerProfile newProfile){
        this.setName(newProfile.getName());
        this.setChannel(newProfile.getChannel());
        this.setPackager(newProfile.getPackager());
        this.setGenericFileName(newProfile.getGenericFileName());
        this.setPort(newProfile.getPort());
        this.setHost(newProfile.getHost());
    }
    public void save(){
        assert getName() != null && !"".equals(getName());
        try {
            if(this.id == null){
                this.id = UUID.randomUUID();
            }
            COLLECTION.put(this.id, this.serialize());
            db.commit();
        }catch (Exception e){
            db.rollback();
            throw new RuntimeException("Unhandled Exception", e);
        }
    }


    public static void delete(UUID id){
        COLLECTION.remove(id);
        db.commit();
    }

    public static List<ServerProfile> findAll(){
        return new ArrayList<>(COLLECTION.getValues().stream().map(ServerProfile::deserialize).collect(Collectors.toList()));
    }

    public ISOPackager isoPackager(){
        try {
            switch (this.getPackager()){
                case GENERIC:
                    return new GenericPackager("./packager/"+getGenericFileName());
                case ISO_1987_ASCII:return new ISO87APackager();
                case ISO_1993_ASCII:return new ISO93APackager();
                case ISO_1987_BINNARY:return new ISO87BPackager();
                case ISO_1993_BINNARY:return new ISO93BPackager();
                case ISO_1987_BITMAP_B: return new ISO87APackagerBBitmap();
            }
        } catch (ISOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ServerProfile deserialize(String strMessage){
        ServerProfile profile = null;
        try {
            profile = om.readValue(strMessage, ServerProfile.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return profile;
    }

    public ServerProfile copy(){
        return ServerProfile.deserialize(this.serialize());
    }

    public String serialize(){
        try {
            return om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unhandled exception", e);
        }
    }

    public BaseChannel createChannel() throws IOException {
        BaseChannel channel = null;
        switch (this.getChannel()){
            case ASCII:channel = createAsciiChannel(); break;
            case BCD:channel = createBcdChannel();break;
            case NAC:channel = createNacChannel();break;
        }
        if(getChannelHeader() != null && !"".equals(getChannelHeader())){
            channel.setHeader(getChannelHeader());
        }
        return channel;
    }

    @NotNull
    private NACChannel createNacChannel() {
        return new NACChannel(this.getHost(), this.getPort(), this.isoPackager(), new byte[2]);
    }

    @NotNull
    private BCDChannel createBcdChannel() {
        return new BCDChannel(this.getHost(),this.getPort(), this.isoPackager(),new byte[2]);
    }

    @NotNull
    private ASCIIChannel createAsciiChannel() {
        return new ASCIIChannel(this.getHost(),this.getPort(), this.isoPackager());
    }

    @Override
    public String toString() {
        return this.getName()+"("+this.getHost()+":"+this.getPort()+","+this.getChannel()+")";
    }

    public String getChannelHeader() {
        return channelHeader.get();
    }

    public StringProperty channelHeaderProperty() {
        return channelHeader;
    }

    public void setChannelHeader(String channelHeader) {
        this.channelHeader.set(channelHeader);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getGenericFileName() {
        return genericFileName.get();
    }

    public StringProperty genericFileNameProperty() {
        return genericFileName;
    }

    public void setGenericFileName(String genericFileName) {
        this.genericFileName.set(genericFileName);
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

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public Integer getPort() {
        return port.get();
    }

    public ObjectProperty<Integer> portProperty() {
        return port;
    }

    public void setPort(Integer port) {
        this.port.set(port);
    }

    public Packager getPackager() {
        return packager.get();
    }

    public ObjectProperty<Packager> packagerProperty() {
        return packager;
    }

    public void setPackager(Packager packager) {
        this.packager.set(packager);
    }

    public Channel getChannel() {
        return channel.get();
    }

    public ObjectProperty<Channel> channelProperty() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel.set(channel);
    }
}
