package com.forest.server.sensors;

import static com.forest.server.SystemData.*;

public class SensorServer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SensorServer <type> <file>");
            System.exit(1);
        }

        String type = args[0];
        String file = args[1];

        if (!type.equals(TEMPERATURE) && !type.equals(HUMIDITY) && !type.equals(FOG)) {
            System.out.println(STR."Invalid sensor type: \{type}");
            System.out.println(STR."Valid types are: \{TEMPERATURE}, \{HUMIDITY}, \{FOG}");
            System.exit(1);
        }

        System.out.println(STR."Type: \{type}");
        System.out.println(STR."File: \{file}");

        SensorInitiator sensorInitiator = new SensorInitiator();
        sensorInitiator.initiateSensors(type, file);
    }
}
