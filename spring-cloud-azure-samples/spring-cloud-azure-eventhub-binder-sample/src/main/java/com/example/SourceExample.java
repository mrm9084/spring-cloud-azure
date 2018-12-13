/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Warren Zhu
 */
@EnableBinding(SourceExample.EventHubSource.class)
@RestController
public class SourceExample {

    @Autowired
    private EventHubSource source;

    @PostMapping("/messages")
    public String postMessage(@RequestParam String message) {
        this.source.output1().send(new GenericMessage<>(message));
        return message;
    }

    public interface EventHubSource {

        String OUTPUT1 = "output1";

        @Output(OUTPUT1)
        MessageChannel output1();

    }
}
