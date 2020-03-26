/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;

public class ConfigListenerTest {

    @Mock
    private AppConfigurationRefresh appConfigurationRefresh;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void throwException() {
        ConfigListener listener = new ConfigListener(appConfigurationRefresh);
        doThrow(new RuntimeException("The listener should swallow all exceptions.")).when(appConfigurationRefresh)
                .refreshConfigurations();
        listener.onApplicationEvent(null);
    }

    @Test
    public void watchEnabledNotConfiguredShouldNotCreateWatch() {
        ConfigListener listener = new ConfigListener(appConfigurationRefresh);
        when(appConfigurationRefresh.refreshConfigurations()).thenReturn(null);
        listener.onApplicationEvent(null);
    }
}