package com.forest.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static org.junit.jupiter.api.Assertions.*;

class FogSensorTest {
    
    private FogSensor fogSensor;

    @BeforeEach
    void setUp() {
        double probabilityCorrect = 0.0;
        double probabilityOutOfRange = 0.0;
        double probabilityError = 0.0;
        
        fogSensor = new FogSensor(probabilityCorrect, probabilityOutOfRange, probabilityError);
    }

    @Test
    void generateReadingCorrect() {
        Double probabilityCorrect = 1.0;

        fogSensor.setProbabilityCorrect(probabilityCorrect);

        // Assert 10000 readings are generated within the correct range
        for (int i = 0; i < 10000; i++) {
            Double reading = fogSensor.generateReading();

            // Print out-of-range readings
            if (reading < 0 || reading > 1 ) {
                System.out.println(reading);
            }

            assertTrue(reading == 0 || reading == 1);
        }
    }

    @Test
    void generateReadingOutOfRangeAndError() {
        Double probabilityOutOfRange = 0.5;
        Double probabilityError = 0.5;

        fogSensor.setProbabilityOutOfRange(probabilityOutOfRange);
        fogSensor.setProbabilityError(probabilityError);

        // Assert 10000 readings are generated within the out-of-range
        for (int i = 0; i < 10000; i++) {
            Double reading = fogSensor.generateReading();

            // Print correct readings
            if (reading == 0 || reading == 1) {
                System.out.println(reading);
            }

            assertEquals(-1, (double) reading);
        }
    }

    @Test
    void generateMessageForProxy() throws InterruptedException {
        // Arrange
        double probabilityCorrect = 1.0;
        fogSensor.setProbabilityCorrect(probabilityCorrect);

        // Act
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5555");
            fogSensor.setSocket(socket);

            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000);
                double reading = fogSensor.generateReading();
                fogSensor.messageForProxy(reading);
            }
        } catch (Exception e) {
            fail(STR."Exception thrown: \{e.getMessage()}");
        }

    }

    @Test
    void threadRun() throws InterruptedException {
        // Arrange
        double probabilityCorrect = 1.0;
        fogSensor.setProbabilityCorrect(probabilityCorrect);

        // Act
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5555");
            fogSensor.setSocket(socket);

            fogSensor.run();
            Thread.sleep(6500);
        } catch (Exception e) {
            fail(STR."Exception thrown: \{e.getMessage()}");
        }
    }
}