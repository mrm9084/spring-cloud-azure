/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author Warren Zhu
 */
@EnableBinding(Processor.class)
@RestController
public class ProcessorExample {

    @Bean
    public Function<String, String> toUpperCase() {
        return String::toUpperCase;
    }
}
