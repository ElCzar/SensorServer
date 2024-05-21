package com.forest.server.sensors;

import com.forest.server.SystemData;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.util.Random;

public class FogSensor extends Sensor implements Runnable{
    public FogSensor(Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        super(probabilityCorrect, probabilityOutOfRange, probabilityError);
        this.SENSOR_COUNT = 3000;
    }

    @Override
    public void run() {
        try(ZContext context = new ZContext(1)) {
            socket = context.createSocket(SocketType.PUSH);
            socket.connect(address);

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
       socket.send(STR."\{SystemData.FOG} \{data}");
    }

    public void generateWarning() {
        System.out.println(STR."Warning: Fog detected!");
        // TODO send warning to proxy, quality control system and signal to aspersor
    }
}
