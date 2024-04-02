package com.forest.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureSensorTest {
    private TemperatureSensor temperatureSensor;

    @BeforeEach
    void setUp() {
        Double probabilityOutOfRange = 0.0;
        Double probabilityError = 0.0;
        Double probabilityCorrect = 0.0;

        temperatureSensor = new TemperatureSensor(null, probabilityCorrect, probabilityOutOfRange, probabilityError);
    }

    @Test
    void generateReadingCorrect() {
        Double probabilityCorrect = 1.0;

        temperatureSensor.setProbabilityCorrect(probabilityCorrect);

        // Assert 10000 readings are generated within the correct range
        for (int i = 0; i < 10000; i++) {
            Double reading = temperatureSensor.generateReading();

            // Print out-of-range readings
            if (reading < 11.0 || reading >= 29.5) {
                System.out.println(reading);
            }

            assertTrue(reading >= 11.0 && reading < 29.5);
        }
    }

    @Test
    void generateReadingOutOfRange() {
        Double probabilityOutOfRange = 1.0;

        temperatureSensor.setProbabilityOutOfRange(probabilityOutOfRange);

        // Assert 10000 readings are generated within the out-of-range
        for (int i = 0; i < 10000; i++) {
            Double reading = temperatureSensor.generateReading();

            // Print correct readings
            if ((reading >= 11.0 && reading <= 29.4) || reading < 1.0 || reading > 50.0) {
                System.out.println(reading);
            }

            assertTrue((reading < 11.0 || reading >= 29.5) && reading >= 1.0 && reading <= 50.0);
        }
    }

    @Test
    void generateReadingError() {
        Double probabilityError = 1.0;

        temperatureSensor.setProbabilityError(probabilityError);

        // Assert 10000 readings are generated with an error
        for (int i = 0; i < 10000; i++) {
            Double reading = temperatureSensor.generateReading();

            // Print correct readings
            if (reading > -1.0 || reading < -50.0) {
                System.out.println(reading);
            }

            assertTrue(reading >= -50.0 && reading <= -1.0);
        }
    }
}