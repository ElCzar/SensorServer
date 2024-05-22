package com.forest.server.sensors.types;

import com.forest.server.SystemData;
import com.forest.server.sensors.MetricsSensors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class HumiditySensor extends Sensor implements Runnable{
    private static final List<Double> humidityRange = List.of(70.0, 100.0);
    private static final Double minHumidity = 1.0;

    public HumiditySensor(Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        super(probabilityCorrect, probabilityOutOfRange, probabilityError);
        this.SENSOR_COUNT = 5000;
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public Double generateReading() {
        // Generate a humidity reading
        double probability = new Random().nextDouble();
        double reading;

        if (probability <= probabilityCorrect) {
            // Between range [70, 100]
            reading = humidityRange.get(0) + (humidityRange.get(1) - humidityRange.get(0)) * new Random().nextDouble();
        } else if (probability <= probabilityCorrect + probabilityOutOfRange) {
            // Between range [1, X)
            reading = minHumidity + (Math.random() * (humidityRange.get(0) - minHumidity - 0.1));
        } else {
            // Between range [-100,-1]
            reading = - (Math.random() * (humidityRange.get(1) - minHumidity) + minHumidity);
        }

        return Math.round(reading * 10.0) / 10.0;
    }

    @Override
    public void messageForProxy(Double data) {
        // Send a message to the proxy
        getSocket().send(STR."\{SystemData.HUMIDITY} \{data} \{LocalDateTime.now()} \{System.currentTimeMillis()}");
        MetricsSensors.prometheusRegistry.counter("sent_requests").increment();
    }
}
