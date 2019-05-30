/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;

/**
 * Options used for set up query to configuration store and control post process after data retrieved from remote.
 */
public class QueryOptions {
    private SettingSelector settingSelector;
    private SettingFields sortField;

    public SettingSelector getSettingSelector() {
        return this.settingSelector;
    }

    public SettingFields getSortField() {
        return this.sortField;
    }

    public QueryOptions withSelector(SettingSelector settingSelector) {
        this.settingSelector = settingSelector;
        return this;
    }

    public QueryOptions withSortField(SettingFields field) {
        this.sortField = field;
        return this;
    }
}
