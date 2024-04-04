package com.forest.server;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public abstract class Sensor implements Runnable{
    protected int SENSOR_COUNT;
    protected Double probabilityCorrect;
    protected Double probabilityOutOfRange;
    protected Double probabilityError;
    protected ZMQ.Socket socket;
    protected static final String address = "tcp://10.43.100.44:5555";


    public Sensor(Double probabilityCorrect, Double probabilityOutOfRange, Double probabilityError) {
        this.probabilityCorrect = probabilityCorrect;
        this.probabilityOutOfRange = probabilityOutOfRange;
        this.probabilityError = probabilityError;
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

    public abstract Double generateReading();

    public abstract void messageForProxy(Double data);

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
