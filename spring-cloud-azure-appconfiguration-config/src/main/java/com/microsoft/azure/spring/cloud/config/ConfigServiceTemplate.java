/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.microsoft.azure.spring.cloud.config.domain.QueryOptions;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionStringPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigServiceTemplate implements ConfigServiceOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceTemplate.class);

    private static final String NULL_LABEL = "%00"; // label=%00 matches null label

    public static final String LOAD_FAILURE_MSG = "Failed to load keys from Azure Config Service.";

    private final ConnectionStringPool connectionStringPool;

    public ConfigServiceTemplate(ConnectionStringPool connectionStringPool) {
        this.connectionStringPool = connectionStringPool;
    }

    @Override
    public List<ConfigurationSetting> getKeys(@NonNull String storeName, @NonNull QueryOptions options) {
        Assert.hasText(storeName, "Config store name should not be null or empty.");
        Assert.notNull(options, "The query options should not be null.");

        return getKeys(options, storeName);
    }

    @Override
    public List<ConfigurationSetting> getRevisions(@NonNull String storeName, @NonNull QueryOptions options) {
        Assert.hasText(storeName, "Config store name should not be null or empty.");
        Assert.notNull(options, "Query options should not be null or empty.");

        return getKeys(options, storeName);
    }

    private List<ConfigurationSetting> getKeys(QueryOptions options, String storeName) {
        ConnectionString connString = connectionStringPool.get(storeName);
        try {
            ConfigurationClientCredentials credentials = new ConfigurationClientCredentials(connString.toString());
            ConfigurationClient client = ConfigurationClient.builder().credentials(credentials).build();

            List<ConfigurationSetting> settings = client.listSettings(options.getSettingSelector());

            if (options.getSortField() != null && options.getSortField().equals(SettingFields.LABEL)) {
                sortByLabel(settings, Arrays.asList(options.getSettingSelector().labels()));
            }

            return settings;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOGGER.error(LOAD_FAILURE_MSG, e);
            // TODO (wp) wrap exception as config service specific exception in order to provide fail-fast etc.
            // features?
            throw new IllegalStateException(LOAD_FAILURE_MSG, e);
        }
    }

    private void sortByLabel(List<ConfigurationSetting> items, List<String> labels) {
        if (items == null || items.size() <= 1 || labels == null || labels.size() <= 1) {
            return;
        }

        Map<String, Integer> labelIndex = new HashMap<>();
        items.sort((o1, o2) -> {
            Integer o1Index = labelIndex.computeIfAbsent(getLabelValue(o1), (t) -> labels.indexOf(t));
            Integer o2Index = labelIndex.computeIfAbsent(getLabelValue(o2), (t) -> labels.indexOf(t));
            return o1Index - o2Index;
        });
    }

    private String getLabelValue(ConfigurationSetting item) {
        if (StringUtils.hasText(item.label())) {
            return item.label();
        }

        return NULL_LABEL;
    }
}
