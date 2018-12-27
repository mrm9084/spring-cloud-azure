/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.integration;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 * Test default bootstrap.yaml properties
 */
@SpringBootTest
public class SpringCloudAzureConfigIT extends AbstractIntegrationTestBase {
    @Autowired
    private Environment environment;

    @Test
    public void byDefaultLoadFromCommonNullLabel() {
        environment.getProperty("test.key").equals("test.key no label");
    }
}
