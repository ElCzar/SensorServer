package com.forest.server.sensors;

import com.forest.server.SystemData;
import com.forest.server.proxy.ProxyServer;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MetricsSensors {
    public static PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    public static Timer qsResponseTime;
    private static final int[] SENSOR_COUNTS = {9099, 9098, 9097};

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java MetricsSensors <sensorType> <probabilityFile>");
            System.exit(1);
        }

        String type = args[0];
        int port;
        switch (type) {
            case SystemData.TEMPERATURE -> port = SENSOR_COUNTS[0];
            case SystemData.HUMIDITY -> port = SENSOR_COUNTS[1];
            case SystemData.FOG -> port = SENSOR_COUNTS[2];
            default -> {
                System.out.println(STR."Invalid sensor type: \{type}");
                System.out.println("Valid types are: temperature, humidity, fog");
                System.exit(1);
                return;
            }
        }

        try {
            registerMetrics();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
            SensorServer.main(args);
        } catch (Exception e) {
            System.out.println("Error creating socket for metrics: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void registerMetrics() {
        // Custom metrics
        // For quality system metrics
        prometheusRegistry.counter("layer_to_qs_request");
        qsResponseTime = Timer.builder("qs_response_time")
                .description("Time taken to get reply from the to the QS")
                .register(prometheusRegistry);
        // For amount of requests sent
        prometheusRegistry.counter("sent_requests");

        // Java and system metrics
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
    }
}
