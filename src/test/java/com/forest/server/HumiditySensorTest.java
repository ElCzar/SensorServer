package com.forest.server;

import com.forest.server.sensors.HumiditySensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static org.junit.jupiter.api.Assertions.*;

class HumiditySensorTest {

    private HumiditySensor humiditySensor;

    @BeforeEach
    void setUp() {
        double probabilityCorrect = 0.0;
        double probabilityOutOfRange = 0.0;
        double probabilityError = 0.0;

        humiditySensor = new HumiditySensor(probabilityCorrect, probabilityOutOfRange, probabilityError);
    }

    @Test
    void generateReadingCorrect() {
        Double probabilityCorrect = 1.0;

        humiditySensor.setProbabilityCorrect(probabilityCorrect);

        // Assert 10000 readings are generated within the correct range
        for (int i = 0; i < 10000; i++) {
            Double reading = humiditySensor.generateReading();

            // Print out-of-range readings
            if (reading < 70.0 || reading > 100.0 ) {
                System.out.println(reading);
            }

            assertTrue(reading >= 70.0 && reading <= 100.0);
        }
    }

    @Test
    void generateReadingOutOfRange() {
        Double probabilityOutOfRange = 1.0;

        humiditySensor.setProbabilityOutOfRange(probabilityOutOfRange);

        // Assert 10000 readings are generated within the out-of-range
        for (int i = 0; i < 10000; i++) {
            Double reading = humiditySensor.generateReading();

            // Print correct readings
            if ((reading >= 70.0 && reading <= 100.0) || reading < 1.0) {
                System.out.println(reading);
            }

            assertTrue(reading >= 1.0 && reading < 70.0);
        }
    }

    @Test
    void generateReadingError() {
        Double probabilityError = 1.0;

        humiditySensor.setProbabilityError(probabilityError);

        // Assert 10000 readings are generated with an error
        for (int i = 0; i < 10000; i++) {
            Double reading = humiditySensor.generateReading();

            // Print correct readings
            if (reading > -1.0 || reading < -100.0) {
                System.out.println(reading);
            }

            assertTrue(reading >= -100.0 && reading <= -1.0);
        }
    }

    @Test
    void generateMessageForProxy() throws InterruptedException {
        // Arrange
        double probabilityCorrect = 1.0;
        humiditySensor.setProbabilityCorrect(probabilityCorrect);

        // Act
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5555");
            humiditySensor.setSocket(socket);

            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000);
                double reading = humiditySensor.generateReading();
                humiditySensor.messageForProxy(reading);
            }
        } catch (Exception e) {
            fail(STR."Exception thrown: \{e.getMessage()}");
        }

    }

    @Test
    void threadRun() throws InterruptedException {
        // Arrange
        double probabilityCorrect = 1.0;
        humiditySensor.setProbabilityCorrect(probabilityCorrect);

        // Act
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5555");
            humiditySensor.setSocket(socket);

            humiditySensor.run();
            Thread.sleep(6500);
        } catch (Exception e) {
            fail(STR."Exception thrown: \{e.getMessage()}");
        }
    }
}