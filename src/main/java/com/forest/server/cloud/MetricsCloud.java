package com.forest.server.cloud;

import com.forest.server.proxy.ProxyServer;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MetricsCloud {
    public static PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public static void main(String[] args) {
        try {
            registerMetrics();
            HttpServer server = HttpServer.create(new InetSocketAddress(9091), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
            CloudServer.main(args);
        } catch (Exception e) {
            System.out.println("Error creating socket for metrics: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void registerMetrics() {
        prometheusRegistry.counter("heartbeat");
        prometheusRegistry.counter("request");
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
    }
}
