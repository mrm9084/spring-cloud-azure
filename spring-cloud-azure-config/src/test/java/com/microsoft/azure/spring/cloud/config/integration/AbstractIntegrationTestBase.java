/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.UrlEscapers;
import com.microsoft.azure.spring.cloud.config.ConfigHttpClient;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;
import net.minidev.json.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

public class AbstractIntegrationTestBase {
    private static final String COMMON_CONN_STRING = System.getenv("COMMON_CONFIG_STORE_CONN_STRING");
    private static final String APP_CONN_STRING = System.getenv("APP_CONFIG_STORE_CONN_STRING");

    private static final String COMMON_DATA_JSON = "/input/properties-common.json";
    private static final String APP_DATA_JSON = "/input/properties-app.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ConfigHttpClient httpClient = new ConfigHttpClient(HttpClients.createDefault());

    @BeforeClass
    public static void initData() throws Exception {
        insertKeys(COMMON_DATA_JSON, COMMON_CONN_STRING);
//        insertKeys(APP_DATA_JSON, APP_CONN_STRING);
    }

    @AfterClass
    public static void clearData() throws Exception {
//        deleteKeys(COMMON_DATA_JSON, COMMON_CONN_STRING);
//        deleteKeys(APP_DATA_JSON, APP_CONN_STRING);
    }

    @Test
    public void mytest() {

    }

    private static void insertKeys(String classPathFile, String connectionString) throws Exception {
        Resource resource = new ClassPathResource(classPathFile);
        List<JSONObject> items = MAPPER.readValue(resource.getFile(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, JSONObject.class));

        for (JSONObject item : items) {
            insertItem(item, connectionString);
        }
    }

    private static void insertItem(JSONObject item, String connectionString) throws Exception {
        ConnectionString connString = ConnectionString.of(connectionString);
        String pathAndQuery = getKeysAPIPath(item, connString);

        StringEntity entity = new StringEntity(item.toJSONString());
        entity.setContentType("application/json");

        HttpPut putRequest = new HttpPut(pathAndQuery);
        putRequest.setEntity(entity);
        putRequest.setHeader("Content-Type", "application/json");

        try {
            CloseableHttpResponse response = httpClient.execute(putRequest, new Date(), connString.getId(),
                    connString.getSecret());

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException("Failed to insert data to config store");
            }
        } finally {
            putRequest.releaseConnection();
        }
    }

    private static void deleteKeys(String classPathFile, String connectionString) throws Exception {
        Resource resource = new ClassPathResource(classPathFile);
        List<JSONObject> items = MAPPER.readValue(resource.getFile(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, JSONObject.class));

        for (JSONObject item : items) {
            deleteItem(item, connectionString);
        }
    }

    private static void deleteItem(JSONObject item, String connectionString) throws Exception {
        ConnectionString connString = ConnectionString.of(connectionString);
        String pathAndQuery = getKeysAPIPath(item, connString);

        HttpDelete delete = new HttpDelete(pathAndQuery);

        try {
            httpClient.execute(delete, new Date(), connString.getId(), connString.getSecret());
        } finally {
            delete.releaseConnection();
        }
    }

    private static String getKeysAPIPath(JSONObject item, ConnectionString connString) throws Exception {
        String pathAndQuery = connString.getEndpoint() + "/kv/" +
                UrlEscapers.urlPathSegmentEscaper().escape(item.getAsString("key"));
        if (StringUtils.hasText(item.getAsString("label"))) {
            pathAndQuery = pathAndQuery + "?label=" + item.getAsString("label");
        }
        return pathAndQuery;
    }
}
