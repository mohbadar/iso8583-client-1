package com.skyworx.iso8583.domain;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ServerProfileTest {
    
    @Test
    public void test_generic_packager(){
        ServerProfile serverProfile = new ServerProfile();
        serverProfile.setChannel(ServerProfile.Channel.ASCII);
        serverProfile.setPackager(ServerProfile.Packager.GENERIC);
        serverProfile.setGenericFileName("sample.xml");
        serverProfile.setHost("localhost");
        serverProfile.setPort(10000);

        try {
            serverProfile.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }
}