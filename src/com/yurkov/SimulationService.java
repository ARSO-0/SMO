package com.yurkov;

import com.yurkov.Components.Source;
import com.yurkov.Entity.Event;
import com.yurkov.Entity.Report;
import com.yurkov.Entity.Request;
import com.yurkov.Entity.SystemState;
import com.yurkov.Managers.BufferManager;
import com.yurkov.Managers.DeviceManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class SimulationService {

    private final Report report;
    private final int totalRequestCount;
    private final ArrayList<Source> sources = new ArrayList<>();
    public final BufferManager bufferManager;
    public final DeviceManager deviceManager;

    public SimulationService(int totalRequests, int sourceCount,
                             double avgGenTime, double genTimeDispersion,
                             int bufferCapacity,
                             int deviceCount, double lambda){
        this.report = new Report(sourceCount, bufferCapacity);
        this.totalRequestCount = totalRequests;
        for (int i = 0; i < sourceCount; i++) {
            sources.add(new Source(avgGenTime, genTimeDispersion));
        }
        this.bufferManager = new BufferManager(bufferCapacity, report, this);
        this.deviceManager = new DeviceManager(deviceCount, lambda, report, this);
        this.simulate();
    }


    private void simulate() {

        ArrayList<Request> temp = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < totalRequestCount; i++) {
            temp.add(sources.get(j).generate());
            j++;
            if (j == sources.size()) {
                j = 0;
            }
        }
        LinkedList<Request> allRequests = temp.stream().sorted(new Request.RequestTimeComparator()).collect(Collectors.toCollection(LinkedList::new));

        double currentTime = 0;
        while (!allRequests.isEmpty()) {
            Request request = allRequests.poll();
            if (request == null) {
                return;
            }
            currentTime = request.getGenerationTime();

            // updating devices and buffer state
            deviceManager.updateDevices(currentTime);

            report.addEvent(new Event(Event.EVENT_TYPE.NEW_REQUEST, request, currentTime, getCurrentSystemState()));

            bufferManager.enqueueRequest(request, currentTime); // buffered event / refused event
            if(deviceManager.isAvailable(currentTime) & !bufferManager.isEmpty()){ // check if request can be processed immediately
                deviceManager.submitRequests(bufferManager.fetchRequest(), currentTime);
            }
        }

        // process remaining requests            1 обработка остатков
        ///   device 1 not loaded!!              2 переработать Request Satats
        while(deviceManager.isWorking()){
            currentTime = deviceManager.getNextEventTime() + 1;

            deviceManager.updateDevices(currentTime);
        }

        report.addDevices(deviceManager.getDevices());
    }

    public SystemState getCurrentSystemState(){
        return new SystemState(bufferManager.getState(), deviceManager.getState());
    }

    public void stepMode(){
        report.stepMode();
    }

    public void printStatistics(){
        report.printStatistics();
    }

}
