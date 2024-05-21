package com.forest.server;

import com.forest.server.sensors.SensorInitiator;
import com.forest.server.sensors.SensorServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensorInitiatorTest {

    private SensorInitiator sensorInitiator;

    @BeforeEach
    void setUp() {
        sensorInitiator = new SensorInitiator();
    }

    @Test
    void correctInitiateSensors() {
        // Arrange
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.PUSH);
            socket.connect("tcp://localhost:5555");
            String file = "src/test/resources/testDataArranged.txt";
            String type = SystemData.TEMPERATURE;

            // Act & Assert
            assertDoesNotThrow(() -> sensorInitiator.initiateSensors(type, file));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void readDataFromFileArranged() {
        // Arrange
        String file = "src/test/resources/testDataArranged.txt";
        List<Double> data;

        // Act & Assert
        data = assertDoesNotThrow(() -> sensorInitiator.readDataFromFile(file));

        assertEquals(3, data.size());
        assertEquals(0.75, data.get(0));
        assertEquals(0.15, data.get(1));
        assertEquals(0.1, data.get(2));
    }

    @Test
    void readDataFromFileUnarranged() {
        // Arrange
        String file = "src/test/resources/testDataUnarranged.txt";
        List<Double> data;

        // Act & Assert
        data = assertDoesNotThrow(() -> sensorInitiator.readDataFromFile(file));

        assertEquals(3, data.size());
        assertEquals(0.75, data.get(0));
        assertEquals(0.15, data.get(1));
        assertEquals(0.1, data.get(2));
    }

    @Test
    void readIncorrectDataFromFile() {
        // Arrange
        String file = "src/test/resources/testDataIncorrect.txt";
        List<Double> data;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> sensorInitiator.readDataFromFile(file));
    }
}