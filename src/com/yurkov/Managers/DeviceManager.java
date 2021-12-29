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

    public void updateDevices(double currentTime){

        while(isAvailable(currentTime)){
            devices.stream().filter(d -> d.isAvailable(currentTime) & d.getCurrentRequest()!=null)
                    .min(Comparator.comparing(Device::getProcessFinishTime)).ifPresent(d -> {
                        d.getCurrentRequest().setProcessedTime(d.getProcessFinishTime());
                        report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_PROCESSED, d.getCurrentRequest(), d.getProcessFinishTime(), ownerService.getCurrentSystemState()));
                        d.reset();
                        Request nextRequest = ownerService.bufferManager.fetchRequest();
                        if(nextRequest != null){
                            double time = d.getProcessFinishTime() + Double.MIN_VALUE;
                            d.processRequest(nextRequest, time);
                            nextRequest.setOnDeviceTime(time);
                            report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_ON_DEVICE, d.getCurrentRequest(), time, ownerService.getCurrentSystemState()));
                        }
            });
            if (ownerService.bufferManager.isEmpty()){
                break;
            }
        }
    }

    public void submitRequests(Request request, double time){
        if(request == null){
            return;
        }
        devices.stream().filter(d -> d.getCurrentRequest() == null).min(Comparator.comparing(Device::getProcessFinishTime))
                .ifPresent(device -> {
                    device.processRequest(request, time);
                    request.setOnDeviceTime(time);
                    report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_ON_DEVICE, device.getCurrentRequest(), time, ownerService.getCurrentSystemState()));
                });
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
        return devices.stream().min(Comparator.comparing(Device::getProcessFinishTime)).get().getProcessFinishTime();
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
