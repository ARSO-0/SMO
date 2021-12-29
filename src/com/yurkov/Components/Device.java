package com.yurkov.Components;

import com.yurkov.Entity.Request;


public class Device {
    private final double lambda;
    private double processFinishTime = -1;
    private Request currentRequest = null;
    private double workingTime = 0;

    public Device(double lambda){
        this.lambda = lambda;
    }

    public Device(Device device){
        this.lambda = device.lambda;
        this.processFinishTime = device.processFinishTime;
        this.currentRequest = device.currentRequest;
        this.workingTime = device.workingTime;
    }

    public void processRequest(Request request, double currentTime){
        currentRequest = request;
        double timeToProcess = (-1/lambda) * Math.log(Math.random());
        processFinishTime = currentTime + timeToProcess;
        workingTime += timeToProcess;
    }

    public boolean isAvailable(double currentTime){
        return currentTime > processFinishTime;
    }

    public double getProcessFinishTime() {
        return processFinishTime;
    }

    public Request getCurrentRequest() {
        return currentRequest;
    }

    public void reset() {
        currentRequest = null;
    }

    public double getWorkingTime() {
        return workingTime;
    }

}
