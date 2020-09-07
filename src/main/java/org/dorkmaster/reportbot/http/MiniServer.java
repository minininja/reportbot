package org.dorkmaster.reportbot.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.dorkmaster.reportbot.config.Config;
import org.dorkmaster.reportbot.util.EventLoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MiniServer {

    private Config config;
    private EventLoggerFactory lf;
    private ObjectMapper om = new ObjectMapper();

    public MiniServer(Config config, EventLoggerFactory lf) {
        this.config = config;
        this.lf = lf;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(config.get("webservice.port").asInt(8080)), 0);
        server.createContext("/messages", httpExchange -> {
            Map<String, Object> content = new HashMap<>();
            content.put("events", lf.getLog().asList());

            byte response[] = om.writeValueAsString(content).getBytes();

            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = httpExchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
    }

}
