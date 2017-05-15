package com.skyworx.iso8583;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executors;

public class EventBus {
    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newSingleThreadExecutor());

    public static void post(Object object){
        eventBus.post(object);
    }

    public static void register(Object object){
        eventBus.register(object);
    }

    public static void unregister(Object object){
        eventBus.unregister(object);
    }
}
