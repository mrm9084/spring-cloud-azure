/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author Warren Zhu
 */
@EnableBinding(SinkExample.EventhubSink.class)
public class SinkExample {

    @StreamListener(EventhubSink.INPUT2)
    public void handleMessage(String message) {
        System.out.println(String.format("New message received: '%s'", message));
    }

    public interface EventhubSink {

        String INPUT2 = "input2";

        @Input(INPUT2)
        SubscribableChannel input2();

    }
}
