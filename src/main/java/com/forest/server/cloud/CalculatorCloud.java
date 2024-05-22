package com.forest.server.cloud;

import com.forest.server.SystemData;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalculatorCloud implements Runnable{
    private static final ArrayList<Double> humidityReadings = new ArrayList<>();
    private static final int DAY_COUNT = 4;
    private static final double HUMIDITY_LIMIT = 70.0;
    private static boolean humidityAverage = false;

    @Override
    public void run() {
        System.out.println("CalculatorCloud running...");
    }

    public void calculateAverageHumidity(double humidity) {
        if (humidityAverage) {
            humidityAverage = false;
            humidityReadings.clear();
        }
        humidityReadings.add(humidity);

        if (!humidityAverage && humidityReadings.size() ==DAY_COUNT) {
            humidityAverage = true;
            double promedio = humidityReadings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.println(STR."Promedio mensual humedad \{LocalDate.now()}: \{promedio}");
            CloudServer.persistencyCloud.clasifyAndSaveData(STR."\{SystemData.MENSUAL} \{SystemData.HUMIDITY} \{promedio} \{LocalDate.now()}");

            // If is below limit create alert
            if (promedio < HUMIDITY_LIMIT) {
                String message = STR."\{SystemData.WARNING} \{SystemData.HUMIDITY} \{promedio} \{LocalDate.now()}";
                CloudServer.persistencyCloud.clasifyAndSaveData(message);
                sendWarningToQualityControl(message);
            }
        }
    }

    private void sendWarningToQualityControl(String message) {
        // TODO Implementa este método para enviar una alerta al sistema de control de calidad - REQUEST-REPLY
        System.out.println(STR."Sending warning to quality control: \{message}");
    }
}
