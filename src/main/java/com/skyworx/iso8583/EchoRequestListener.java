package com.skyworx.iso8583;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

import java.io.IOException;

public class EchoRequestListener implements ISORequestListener {
    @Override
    public boolean process(ISOSource isoSource, ISOMsg isoMsg) {
        try {
            isoMsg.setResponseMTI();
            isoSource.send(isoMsg);
        } catch (IOException | ISOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
