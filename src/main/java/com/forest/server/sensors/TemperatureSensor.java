package com.forest.server.sensors;

import com.forest.server.SystemData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class TemperatureSensor extends Sensor implements Runnable{
    private static final List<Double> temperatureRange = List.of(11.0, 29.4);
    private static final Double minTemperature = 1.0;
    private static final Double maxTemperature = 50.0;

    public TemperatureSensor(Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        super(probabilityCorrect, probabilityOutOfRange, probabilityError);
        this.SENSOR_COUNT = 6000; // Milliseconds to seconds
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public Double generateReading() {
        // Generate a temperature reading
        double probability = new Random().nextDouble();
        double reading;

        if (probability <= probabilityCorrect) {
            reading = temperatureRange.get(0) + (temperatureRange.get(1) - temperatureRange.get(0)) * new Random().nextDouble();
        } else if (probability <= probabilityCorrect + probabilityOutOfRange) {
            // Between range [1, X) (Y, 50]
            int firstOrSecond = new Random().nextInt(2);

            if (firstOrSecond == 0) {
                reading = minTemperature + (Math.random() * (temperatureRange.get(0) - minTemperature - 0.1));
            } else {
                reading = temperatureRange.get(1) + (Math.random() * (maxTemperature - temperatureRange.get(1) - 0.1)) + 0.1;
            }
        } else {
            // Between range [-50,-1]
            reading = - (Math.random() * (maxTemperature - minTemperature) + minTemperature);
        }

        return Math.round(reading * 10.0) / 10.0;
    }

    @Override
    public void messageForProxy(Double data) {
        // Send a message to the proxy
        socket.send(STR."\{SystemData.TEMPERATURE} \{data} \{LocalDateTime.now()}");
    }
}
