package com.yurkov.Entity;

import java.util.Comparator;

public class Request {

    private final int sourceNumber;
    private final int requestNumber;
    private final double generationTime;
    private double bufferedTime;
    private double onDeviceTime;
    private double processedTime;
    private boolean isRefused;

    public Request(double generatedTime, int sourceNumber, int requestNumber) {
        this.generationTime = generatedTime;
        this.sourceNumber = sourceNumber;
        this.requestNumber = requestNumber;
    }

    public double getGenerationTime()
    {
        return generationTime;
    }

    public int getSourceNumber()
    {
        return sourceNumber;
    }

    public int getRequestNumber()
    {
        return requestNumber;
    }

    public double getBufferedTime() {
        return bufferedTime;
    }

    public void setBufferedTime(double bufferedTime) {
        this.bufferedTime = bufferedTime;
    }

    public double getOnDeviceTime() {
        return onDeviceTime;
    }

    public void setOnDeviceTime(double onDeviceTime) {
        this.onDeviceTime = onDeviceTime;
    }

    public double getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(double processedTime) {
        this.processedTime = processedTime;
    }

    public boolean isRefused() {
        return isRefused;
    }

    public void setRefused(boolean refused) {
        isRefused = refused;
        bufferedTime = -1;
        onDeviceTime = -1;
        processedTime = -1;
    }

    @Override
    public String toString()
    {
        return sourceNumber + " " + requestNumber;
    }


    public static class RequestComparator implements Comparator<Request> {

        @Override
        public int compare(Request o1, Request o2) {
            if(o1.getSourceNumber() < o2.getSourceNumber()){
                return 1;
            }
            else if(o1.getSourceNumber() > o2.getSourceNumber()){
                return -1;

            }else{
                if(o1.getGenerationTime() > o2.getGenerationTime()){
                    return 1;
                }
                if(o1.getGenerationTime() < o2.getGenerationTime()){
                    return -1;
                }
                if(o1.getGenerationTime() == o2.getGenerationTime()){
                    return 0;
                }
            }
            return 0;
        }
    }

    public static class RequestTimeComparator implements Comparator<Request> {

        @Override
        public int compare(Request o1, Request o2) {
            return Double.compare(o1.getGenerationTime(), o2.getGenerationTime());
        }
    }
}
