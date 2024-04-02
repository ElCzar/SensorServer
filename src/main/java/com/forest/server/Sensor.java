package com.forest.server;

import org.zeromq.ZMQ;

public abstract class Sensor extends Thread{
    protected int SENSOR_COUNT;
    protected Double probabilityCorrect;
    protected Double probabilityOutOfRange;
    protected Double probabilityError;
    protected ZMQ.Socket socket;


    public Sensor(ZMQ.Socket socket, Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        this.socket = socket;
        this.probabilityCorrect = probabilityCorrect;
        this.probabilityOutOfRange = probabilityOutOfRange;
        this.probabilityError = probabilityError;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Sensor reading...");
        }
    }

    public abstract Double generateReading();

    public abstract void messageForProxy();

    public Double getProbabilityCorrect() {
        return probabilityCorrect;
    }

    public void setProbabilityCorrect(Double probabilityCorrect) {
        this.probabilityCorrect = probabilityCorrect;
    }

    public Double getProbabilityOutOfRange() {
        return probabilityOutOfRange;
    }

    public void setProbabilityOutOfRange(Double probabilityOutOfRange) {
        this.probabilityOutOfRange = probabilityOutOfRange;
    }

    public Double getProbabilityError() {
        return probabilityError;
    }

    public void setProbabilityError(Double probabilityError) {
        this.probabilityError = probabilityError;
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }

    public void setSocket(ZMQ.Socket socket) {
        this.socket = socket;
    }
}
