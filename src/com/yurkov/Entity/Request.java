package com.yurkov.Entity;

import java.util.Comparator;

public class Request {

    private final double generationTime;
    private final int sourceNumber;
    private final int requestNumber;

    public Request(double generatedTime, int sourceNumber, int requestNumber) {
        this.generationTime = generatedTime;
        this.sourceNumber = sourceNumber;
        this.requestNumber = requestNumber;
    }

    public Request(Request request) {
        this.generationTime = request.getGenerationTime();
        this.sourceNumber = request.sourceNumber;
        this.requestNumber = request.requestNumber;
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
