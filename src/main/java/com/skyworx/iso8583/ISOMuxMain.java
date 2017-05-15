package com.skyworx.iso8583;

import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;

import java.util.concurrent.TimeUnit;

public class ISOMuxMain {
    public static void main(String[] args) throws InterruptedException {
        ASCIIChannel asciiChannel = new ASCIIChannel("localhost",10000, new ISO87APackager());
        ISOMUX isomux = new ISOMUX(asciiChannel);
        isomux.setISORequestListener((source, m) -> {
            m.dump(System.out, "");
            return false;
        });
        new Thread(isomux).start();
        while (!isomux.isConnected()){
            TimeUnit.SECONDS.sleep(3);
        }
        ISOMsg m = new ISOMsg("0800");
        m.set(11, "9999999");
        m.dump(System.out, "");
        isomux.send(m);
    }
}
