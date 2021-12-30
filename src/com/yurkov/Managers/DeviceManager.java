package com.yurkov.Managers;

import com.yurkov.Components.Device;
import com.yurkov.Entity.Event;
import com.yurkov.Entity.Report;
import com.yurkov.Entity.Request;
import com.yurkov.SimulationService;

import java.util.ArrayList;
import java.util.Comparator;

public class DeviceManager {

    private final ArrayList<Device> devices;
    private final Report report;
    private final SimulationService ownerService;

    public DeviceManager(int deviceCount, double lambda, Report report, SimulationService ownerService){
        this.devices = new ArrayList<>();
        for (int i = 0; i < deviceCount; i++) {
            devices.add(new Device(lambda));
        }
        this.report = report;
        this.ownerService = ownerService;
    }

    public void updateDevices(){
        Device device = devices.stream().filter(d -> d.getCurrentRequest() != null).min(Comparator.comparing(Device::getProcessFinishTime)).orElse(null);
        if(device == null){
            return;
        }
        double eventTime = device.getProcessFinishTime();

        device.getCurrentRequest().setProcessedTime(eventTime);
        report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_PROCESSED, device.getCurrentRequest(), eventTime, ownerService.getCurrentSystemState()));
        device.setCurrentRequest(null);

        if(!ownerService.bufferManager.isEmpty()){
            Request request = ownerService.bufferManager.fetchRequest();
            request.setOnDeviceTime(device.getProcessFinishTime() + Double.MIN_VALUE);
            device.processRequest(request, device.getProcessFinishTime()+Double.MIN_VALUE);
            report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_ON_DEVICE, request,
                    request.getOnDeviceTime(), ownerService.getCurrentSystemState()));
        }
    }

    public void submitRequests(Request request, double time){
        if(request == null){
            return;
        }
        Device device = devices.stream().filter(d -> d.getCurrentRequest() == null).findFirst().orElse(null);
        if(device == null){
            return;
        }
        device.processRequest(request, time);
        request.setOnDeviceTime(time);
        report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_ON_DEVICE, device.getCurrentRequest(), time, ownerService.getCurrentSystemState()));
    }


    public boolean isAvailable(){
        for(Device d : devices){
            if(d.isAvailable()){
                return true;
            }
        }
        return false;
    }

    public boolean isWorking(){
        for(Device d : devices){
            if(d.getCurrentRequest() != null){
                return true;
            }
        }
        return false;
    }

    public double getNextEventTime(){
        Device device = devices.stream().filter(d -> d.getCurrentRequest() != null).min(Comparator.comparing(Device::getProcessFinishTime)).orElse(null);
        if(device == null){
            return Double.MAX_VALUE;
        }
        return device.getProcessFinishTime();
    }

    public ArrayList<Device> getState() {
        ArrayList<Device> temp = new ArrayList<>();
        for(Device d : devices){
            Device device = new Device(d);
            temp.add(device);
        }
        return temp;
    }

    public ArrayList<Device> getDevices(){
        return new ArrayList<>(devices);
    }

}
