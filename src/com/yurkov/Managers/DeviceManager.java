package com.yurkov.Managers;

import com.yurkov.Components.Device;
import com.yurkov.Entity.Event;
import com.yurkov.Entity.Report;
import com.yurkov.Entity.Request;
import com.yurkov.SimulationService;

import java.util.ArrayList;

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

    public void updateDevices(double currentTime){
        for(Device d : devices){
            if(d.isAvailable(currentTime) & d.getCurrentRequest()!=null){
                report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_PROCESSED, d.getCurrentRequest(), d.getProcessFinishTime(), ownerService.getCurrentSystemState()));
                d.setCurrentRequest(null);
            }
        }
    }

    public void submitRequest(Request request, double currentTime){
        if(request == null){
            return;
        }
        for (Device device : devices) {
            if (device.getCurrentRequest() == null) {
                device.processRequest(request, currentTime);
                report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_ON_DEVICE, request, currentTime, ownerService.getCurrentSystemState()));
                break;
            }
        }
    }

    public boolean isAvailable(double currentTime){
        for(Device d : devices){
            if(d.isAvailable(currentTime)){
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
        int minIndex = 0;
        for(Device d : devices){
            if(d.getCurrentRequest() != null){
                if(d.getProcessFinishTime() < devices.get(minIndex).getProcessFinishTime()){
                    minIndex = devices.indexOf(d);
                }
            }
        }
        return devices.get(minIndex).getProcessFinishTime();
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
