package com.forest.server.sensors.types;

import com.forest.server.SystemData;
import com.forest.server.sensors.AddressListener;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.Random;

public class FogSensor extends Sensor implements Runnable{
    private ZMQ.Socket sprinklerSocket;
    private ZMQ.Socket qualitySocket;
    public FogSensor(Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        super(probabilityCorrect, probabilityOutOfRange, probabilityError);
        this.SENSOR_COUNT = 3000;
    }

    @Override
    public void run() {
        try(ZContext context = new ZContext(1)) {
            sprinklerSocket = context.createSocket(SocketType.PUSH);
            sprinklerSocket.connect("tcp://localhost:5590");

            qualitySocket = context.createSocket(SocketType.REQ);
            qualitySocket.connect("tcp://localhost:5560");

            addressListener = new AddressListener();
            Thread listenerThread = new Thread(addressListener);
            listenerThread.start();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(SENSOR_COUNT);
                    double reading = generateReading();

                    if (reading == 1) generateWarning();
                    messageForProxy(reading);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (Exception e) {
            System.out.println(STR."Error creating socket: \{e.getMessage()}");
            System.exit(1);
        }
    }

    @Override
    public Double generateReading() {
        // Generate a fog reading
        double probability = new Random().nextDouble();
        double reading;

        if (probability <= probabilityCorrect) {
            // 0 or 1
            reading = new Random().nextInt(2);
        } else  {
            reading = -1;
        }

        return reading;
    }

    @Override
    public void messageForProxy(Double data) {
       getSocket().send(STR."\{SystemData.FOG} \{data} \{LocalDateTime.now()}");
    }

    private void sprinklerSignal() {
        sprinklerSocket.send(STR."Turn on sprinkler!");
    }

    public void generateWarning() {
        String message = STR."\{SystemData.WARNING} \{SystemData.FOG} 1 \{LocalDateTime.now()}";
        // Sends to proxy
        getSocket().send(message);
        // Sends to aspersor
        sprinklerSignal();
        // Sends to quality system
        sendQualitySystem(message);
    }

    private void sendQualitySystem(String message) {
        qualitySocket.send(message);
        byte[] reply = qualitySocket.recv();
        System.out.println(STR."Success: [\{new String(reply, ZMQ.CHARSET)}]");
    }
}
