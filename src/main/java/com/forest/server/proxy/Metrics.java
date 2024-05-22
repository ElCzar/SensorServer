package com.forest.server.proxy;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Metrics {
    public static PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public static void main(String[] args) {
        try {
            // Creates 2 metrics, for heartbeat and for amount of request
            prometheusRegistry.counter("heartbeat");
            prometheusRegistry.counter("request");

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
            ProxyServer.main(args);
        } catch (Exception e) {
            System.out.println("Error creating socket for metrics: " + e.getMessage());
            System.exit(1);
        }
    }
}
