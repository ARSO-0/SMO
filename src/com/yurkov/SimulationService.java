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
    private final BufferManager bufferManager;
    private final DeviceManager deviceManager;

    public SimulationService(int totalRequests, int sourceCount,
                             double avgGenTime, double genTimeDispersion,
                             int bufferCapacity,
                             int deviceCount, double lambda){
        this.report = new Report(sourceCount, bufferCapacity, deviceCount, totalRequests);
        this.totalRequestCount = totalRequests;
        for (int i = 0; i < sourceCount; i++) {
            sources.add(new Source(avgGenTime, genTimeDispersion));
        }
        this.bufferManager = new BufferManager(bufferCapacity, report, this);
        this.deviceManager = new DeviceManager(deviceCount, lambda, report, this);
    }


    public void simulate() {

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
            report.addEvent(new Event(Event.EVENT_TYPE.NEW_REQUEST, request, currentTime, getCurrentSystemState()));     // new request event
            deviceManager.updateDevices(currentTime); // finished events

            Request reqFromBuffer = null;
            while (deviceManager.isAvailable(currentTime)) {
                reqFromBuffer = bufferManager.fetchRequest();
                if (reqFromBuffer == null) { // -> buffer empty
                    break;
                }
                deviceManager.submitRequest(reqFromBuffer, currentTime);  // on device events
            }

            bufferManager.enqueueRequest(request, currentTime); // refused event / moved to buffer events

            if(deviceManager.isAvailable(currentTime)) { // checking if request can be submitted immediately
                reqFromBuffer = bufferManager.fetchRequest();
                if (reqFromBuffer != null) {
                    deviceManager.submitRequest(reqFromBuffer, currentTime);
                }
            }
        }

        // process remaining requests
        while(deviceManager.isWorking()){
            currentTime = deviceManager.getNextEventTime() + 1;
            deviceManager.updateDevices(currentTime);

            Request reqFromBuffer = null;
            while (deviceManager.isAvailable(currentTime)) {
                reqFromBuffer = bufferManager.fetchRequest();
                if (reqFromBuffer == null) { // -> buffer empty
                    break;
                }
                deviceManager.submitRequest(reqFromBuffer, currentTime);  // on device events
            }
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
