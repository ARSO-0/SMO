package com.yurkov.Components;

import com.yurkov.Entity.Request;

public class Source {

    public static int requestCount;
    public static int sourceCount;
    private final int sourceNumber;
    private final double avgGenTime;
    private final double genDispersion;
    private double previousRequestTime=0;

    public Source(double avgGenTime, double genDispersion){
        sourceCount++;
        this.sourceNumber=sourceCount;
        this.avgGenTime = avgGenTime;
        this.genDispersion = genDispersion;
    }

    public Request generate(){
        double generationTime = previousRequestTime + avgGenTime + genDispersion * (Math.random()-0.5);
        requestCount++;
        previousRequestTime = generationTime;
        return new Request(generationTime, sourceNumber, requestCount);
    }
}
