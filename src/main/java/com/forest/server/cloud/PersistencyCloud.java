package com.forest.server.cloud;

import com.forest.server.SystemData;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersistencyCloud {
    private final ExecutorService executorService;
    private final MongoCollection<Document> averageCollection;
    private final MongoCollection<Document> measurementCollection;
    private final MongoCollection<Document> warningsCollection;

    public PersistencyCloud() {
        this.executorService = Executors.newCachedThreadPool();

        // Connect to mongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("cloud");

        // Create collections
        this.averageCollection = database.getCollection("average");
        this.measurementCollection = database.getCollection("measurements");
        this.warningsCollection = database.getCollection("warnings");
    }

    public void clasifyAndSaveData(String information) {
        executorService.submit(() -> {
            // Separate data by spaces
            String[] parts = information.split(" ");
            String infoType = parts[0];
            String sensorType = parts[1];
            double value = Double.parseDouble(parts[2]);
            String time = parts[3];

            // Save data to database
            System.out.println(STR."Saving data to database: \{infoType} \{sensorType} \{value}");

            // Use mongoDB to save data
            switch (infoType) {
                case SystemData.AVERAGE:
                    saveAverageData("Daily", sensorType, value, time);
                    break;
                case SystemData.MENSUAL:
                    saveAverageData("Monthly", sensorType, value, time);
                    break;
                case SystemData.MEASUREMENT:
                    saveMeasurementData(sensorType, value, time);
                    break;
                case SystemData.WARNING:
                    saveWarningData(sensorType, value, time);
                    break;
                default:
                    System.out.println("Invalid info type");
            }
        });
    }

    public void saveAverageData(String type, String sensorType, double value, String time) {
       Document document = new Document("type", type)
               .append("sensorType", sensorType)
               .append("value", value)
                .append("time", time);
       averageCollection.insertOne(document);
    }

    public void saveMeasurementData(String sensorType, double value, String time) {
        Document document = new Document("sensorType", sensorType)
                .append("value", value)
                .append("time", time);
        measurementCollection.insertOne(document);
    }

    public void saveWarningData(String sensorType, double value, String time) {
        Document document = new Document("sensorType", sensorType)
                .append("value", value)
                .append("time", time);
        warningsCollection.insertOne(document);
    }

    private void sendWarningToQualityControl(String message) {
        // TODO Implementa este método para enviar una alerta al quality control system - REQUEST-REPLY
        System.out.println(STR."Sending message to cloud: \{message}");
    }
}
