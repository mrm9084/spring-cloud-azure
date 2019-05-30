/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.LABEL_SEPARATOR;
import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest({ ConfigServiceTemplate.class, ConfigurationClient.class })
public class ConfigServiceTemplateTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigurationClient configClient;

    private ConfigServiceTemplate template;

    private ConnectionStringPool pool;
    private ConfigStore configStore;
    public  List<ConfigurationSetting> testItems;

    private static final ConfigurationSetting item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1);
    private static final ConfigurationSetting item2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2);
    private static final ConfigurationSetting item3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3);

    private static final SettingSelector selector = new SettingSelector().keys(TEST_CONTEXT);
    private static final QueryOptions TEST_OPTIONS = new QueryOptions().withSelector(selector);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        pool = new ConnectionStringPool();
        configStore = new ConfigStore();
        testItems = new ArrayList<>();

        pool.put(TEST_STORE_NAME, ConnectionString.of(TEST_CONN_STRING));
        configStore.setName(TEST_STORE_NAME);
        configStore.setConnectionString(TEST_CONN_STRING);

        testItems.addAll(Arrays.asList(item1, item2, item3));
    }

    @Test
    public void testKeysCanBeSearched() {
        when(configClient.listSettings(any())).thenReturn(testItems);
        template = new ConfigServiceTemplate(pool);

        List<ConfigurationSetting> result = template.getKeys(configStore.getName(), TEST_OPTIONS);
        assertThat(result.size()).isEqualTo(testItems.size());
        assertThat(result).containsExactlyInAnyOrder(testItems.stream().toArray(ConfigurationSetting[]::new));
    }

    @Test
    public void testSpecificLabelCanBeSearched() {
        prepareConfigClient();

        template = new ConfigServiceTemplate(pool);
        SettingSelector selector = TEST_OPTIONS.getSettingSelector().labels(TEST_LABEL_2);
        TEST_OPTIONS.withSelector(selector);
        List<ConfigurationSetting> result = template.getKeys(configStore.getName(), TEST_OPTIONS);

        List<ConfigurationSetting> expectedResult = Arrays.asList(item2);
        assertThat(result.size()).isEqualTo(expectedResult.size());
        assertThat(result).containsExactly(expectedResult.stream().toArray(ConfigurationSetting[]::new));
    }

    private void prepareConfigClient() {
        when(configClient.listSettings(any())).thenAnswer(new Answer<List<ConfigurationSetting>>() {
            @Override
            public List<ConfigurationSetting> answer(InvocationOnMock invocation) throws Throwable {
                // Extract label params from the request argument and filter result from the given testItems
                Object[] args = invocation.getArguments();
                HttpUriRequest request = (HttpUriRequest) args[0];
                List<NameValuePair> params = URLEncodedUtils.parse(request.getURI(), Charset.defaultCharset());
                Optional<NameValuePair> labelParam = params.stream()
                        .filter(p -> LABEL_PARAM.equals(p.getName())).findFirst();

                if (!labelParam.isPresent()) {
                    return null;
                }

                String labelValue = labelParam.get().getValue();
                List<String> labels = Arrays.asList(labelValue.split(LABEL_SEPARATOR));

                return testItems.stream().filter(item -> labels.contains(item.label())).collect(Collectors.toList());
            }
        });
    }
}
