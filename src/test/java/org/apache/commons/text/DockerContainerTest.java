/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.sun.net.httpserver.HttpServer;




/**
 * Tests for the DockerContainer class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DockerContainerTest {

    /** The HTTP server. */
    private HttpServer server;
    /** The HTTP port. */
    private static final int HTTP_PORT = 8080;
    /** The status code for OK. */
    private static final int STATUS_OK = 200;
    /** The status code for error. */
    private static final int STATUS_ERROR = 404;
    /** The status code for method not valid. */
    private static final int METHOD_NOT_VALID = 405;

    /**
     * Starts the HTTP server.
     * @throws IOException
     */
    @BeforeAll
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/run-method", new MethodHandler());
        server.createContext("/", new DockerContainer.HtmlHandler());
        server.setExecutor(null);
        server.start();
    }

    /**
     * Stops the HTTP server.
     */
    @AfterAll
    void stopServer() {
        server.stop(0);
    }

    /**
     * Tests the method handler with the initials method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_initialsMethod() throws IOException {
        String requestBody = "{\"method\":\"initials\",\"inputText\":\"John Doe\"}";

        String response = sendPostRequest("/run-method", requestBody);
        assertTrue(response.contains("\"result\":\"JD\""));
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_capitalizeMethod() throws IOException {
        String requestBody = "{\"method\":\"capitalize\",\"inputText\":\"hello world\"}";

        String response = sendPostRequest("/run-method", requestBody);
        assertTrue(response.contains("\"result\":\"Hello World\""));
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_randomMethod() throws IOException {
        String requestBody = "{\"method\":\"random\",\"inputText\":\"10\"}";

        String response = sendPostRequest("/run-method", requestBody);
        assertTrue(response.contains("\"result\":\""));
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_unknownMethod() throws IOException {
        String requestBody = "{\"method\":\"unknown\",\"inputText\":\"hello world\"}";

        String response = sendPostRequest("/run-method", requestBody);
        assertTrue(response.contains("\"result\":\"Unknown method: unknown\""));
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_missingInput() throws IOException {
        String requestBody = "{\"method\":\"wrap\"}";

        String response = sendPostRequest("/run-method", requestBody);
        assertTrue(response.contains("\"result\":\"Error no input provided!\""));
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testMethodHandler_invalidMethod() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/run-method").openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        assertEquals(METHOD_NOT_VALID, responseCode);
    }

    /**
     * Tests the method handler with the wrap method.
     * @throws IOException
     */
    @Test
    void testParseJson() {
        MethodHandler handler = new MethodHandler();
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Map<String, String> parsed = handler.parseJson(json);

        assertEquals("value1", parsed.get("key1"));
        assertEquals("value2", parsed.get("key2"));
    }

    /**
     * Sends a POST request to the specified endpoint with the given JSON body.
     * @param endpoint
     * @param jsonBody
     * @return
     * @throws IOException
     */
    private String sendPostRequest(String endpoint, String jsonBody) throws IOException {
        URL url = new URL("http://localhost:8080" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        InputStream is = connection.getInputStream();
        return readStream(is);
    }

    /**
     * Reads the input stream and returns the content as a string.
     * @param is
     * @return
     * @throws IOException
     */
    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
