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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class DockerContainer {
    /** The HTTP port. */
    static final int HTTP_PORT = 8080;
    /** The status code for OK. */
    static final int STATUS_OK = 200;
    /** The status code for error. */
    static final int STATUS_ERROR = 404;
    /** The status code for method not valid. */
    static final int METHOD_NOT_VALID = 405;
    /** The byte buffer size. */
    static final int BYTE_BUFFER_SIZE = 1024;
    /** The wrap length. */
    static final int WRAP_LENGTH = 20;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);

        // Context for API
        server.createContext("/run-method", new MethodHandler());

        // Context for serving index.html
        server.createContext("/", new HtmlHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + HTTP_PORT);
    }

    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Path to your HTML file (make sure it's in the correct location in your project)
            Path htmlFilePath = Paths.get("src/main/resources/static/index.html");

            // Check if the file exists
            if (Files.exists(htmlFilePath)) {
                byte[] htmlContent = Files.readAllBytes(htmlFilePath); // Read the HTML content from the file
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(STATUS_OK, htmlContent.length);
                OutputStream os = exchange.getResponseBody();
                os.write(htmlContent); // Send the HTML content to the client
                os.close();
            } else {
                htmlFilePath = Paths.get("static/index.html");
                if (Files.exists(htmlFilePath)) {
                    byte[] htmlContent = Files.readAllBytes(htmlFilePath); // Read the HTML content from the file
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(STATUS_OK, htmlContent.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(htmlContent); // Send the HTML content to the client
                    os.close();
                } else {
                    // If the HTML file is not found, send a 404 response
                    String response = "<h1>404 - File Not Found</h1>";
                    exchange.sendResponseHeaders(STATUS_ERROR, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        }
    }

        private static byte[] readFile(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[BYTE_BUFFER_SIZE];
                while ((nRead = fis.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }

    class MethodHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String requestBody = readInputStream(exchange.getRequestBody());
                Map<String, String> jsonRequest = parseJson(requestBody);

                String method = jsonRequest.getOrDefault("method", "");
                String inputText = jsonRequest.getOrDefault("inputText", "");

                String result;
                if (inputText.isEmpty()) {
                    result = "Error no input provided!";
                } else {
                    switch (method) {
                        case "wrap":
                            result = WordUtils.wrap(inputText, DockerContainer.WRAP_LENGTH);
                            result = StringEscapeUtils.escapeJson(result);
                            System.out.println(method + " " + result);
                            break;
                        case "initials":
                            result = WordUtils.initials(inputText);
                            System.out.println(method + " " + result);
                            break;
                        case "capitalize":
                            result = WordUtils.capitalize(inputText);
                            System.out.println(method + " " + result);
                            break;
                       case "random":
                            try {
                                int length = Integer.parseInt(inputText);
                                result = RandomStringGenerator.builder().get().generate(length);
                                System.out.println(method + " " + result);
                            } catch (NumberFormatException e) {
                                result = "Error: inputText must be a number!";
                            }
                            break;
                        default:
                            result = "Unknown method: " + method;
                            System.out.println(method + " " + result);
                    }
                }

                System.out.println(method + "2 " + result);

                String jsonResponse = "{\"result\":\"" + result.replace("\"", "\\\"") + "\"}";

                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(DockerContainer.STATUS_OK, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(DockerContainer.METHOD_NOT_VALID, -1); // Method Not Allowed
            }
        }

        private String readInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[DockerContainer.BYTE_BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        }

        protected Map<String, String> parseJson(String json) {
            Map<String, String> map = new HashMap<>();
            json = json.trim().replace("{", "").replace("}", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    map.put(key, value);
                }
            }
            return map;
        }
    }

