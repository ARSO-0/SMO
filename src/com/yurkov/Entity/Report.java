package com.yurkov.Entity;

import com.yurkov.Components.Device;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class Report {
    private ArrayList<Event> events = new ArrayList<>();
    private final ArrayList<Request> allRequests = new ArrayList<>();
    private ArrayList<Device> devices = null;
    private final int sourceCount;
    private final int bufferCapacity;


    public Report(int sourceCount, int bufferCapacity) {
        this.sourceCount = sourceCount;
        this.bufferCapacity = bufferCapacity;
    }

    public void addEvent(Event event){
        events.add(event);
    }

    public void stepMode(){
        events = events.stream().sorted(Comparator.comparing(Event::getEventTime)).collect(Collectors.toCollection(ArrayList::new));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'n' for next step or 'q' to quit");
        for(Event event : events){
            String command = scanner.nextLine();
            if(command.equals("q")){
                break;
            }
            if(command.equals("n")) {
                System.out.println("\n" + event.getType() + " Request " + event.getRequest().getRequestNumber() + " at time: " + String.format("%.3f",event.eventTime()));
                ArrayList<Request> buffer = event.getSystemState().getBufferState();
                ArrayList<Device> devices = event.getSystemState().getDeviceState();

                System.out.format("+----------+------------+------------+----------+------------+%n");
                System.out.format("| Sources  |            | Buffer     | Devices  |            |%n");
                System.out.format("+----------+------------+------------+----------+------------+%n");
                int tableSize = Math.max(Math.max(sourceCount, devices.size()), event.getSystemState().getBufferState().size());
                for (int i = 0; i < tableSize; i++) {
                    StringBuilder data = new StringBuilder();


                    if (sourceCount > i) {
                        data.append(String.format("| %-8s |", "Source " + i));
                        if (event.getType() == Event.EVENT_TYPE.NEW_REQUEST) {
                            if (event.getRequest().getSourceNumber() - 1 == i) {
                                data.append(String.format(" %-10s ", "<Req " + event.getRequest().getRequestNumber() + ">"));
                            } else {
                                data.append(String.format(" %-10s ", "---"));
                            }
                        } else if (event.getType() == Event.EVENT_TYPE.REQUEST_REFUSED) {
                            if (event.getRequest().getSourceNumber() - 1 == i) {
                                data.append(String.format(" %-10s ", "!Req " + event.getRequest().getRequestNumber() + "!"));
                            } else {
                                data.append(String.format(" %-10s ", "---"));
                            }
                        } else {
                            data.append(String.format(" %-10s ", "---"));
                        }
                    } else {
                        data.append(String.format("| %-8s | %-10s ", "", ""));
                    }

                    if (bufferCapacity > i) {
                        if (i < buffer.size()) {
                            if (buffer.get(i) != null) {
                                if (event.getType() == Event.EVENT_TYPE.REQUEST_BUFFERED
                                        & event.getRequest().getRequestNumber() == buffer.get(i).getRequestNumber()) {
                                    data.append(String.format("| %-10s |", "->Req " + buffer.get(i).getRequestNumber()));
                                } else {
                                    data.append(String.format("| %-10s |", "Req " + buffer.get(i).getRequestNumber()));
                                }
                            }
                        } else {
                            data.append(String.format("| %-10s |", "---"));
                        }
                    } else {
                        data.append(String.format("| %-10s |", "---"));
                    }

                    if (devices.size() > i) {
                        data.append(String.format(" %-8s ", "Device " + i));
//
                        if (devices.get(i).getCurrentRequest() != null) {
                            if (devices.get(i).getCurrentRequest().getRequestNumber() == event.getRequest().getRequestNumber()) {
                                if (event.getType() == Event.EVENT_TYPE.REQUEST_PROCESSED) {
                                    data.append(String.format("| %-10s |", "<Req " + devices.get(i).getCurrentRequest().getRequestNumber() + ">"));
                                } else if (event.getType() == Event.EVENT_TYPE.REQUEST_ON_DEVICE) {
                                    data.append(String.format("| %-10s |", "->Req " + devices.get(i).getCurrentRequest().getRequestNumber()));
                                }
                            } else {
                                data.append(String.format("| %-10s |", "Req " + devices.get(i).getCurrentRequest().getRequestNumber()));
                            }
                        } else {
                            data.append(String.format("| %-10s |", "free"));
                        }
                    } else {
                        data.append(String.format(" %-8s | %-10s |", "", ""));
                    }
                    System.out.println(data);
                }
                System.out.format("+----------+------------+------------+----------+------------+%n");
            }
        }
    }

    public void printStatistics(){

        for(Event event : events){
            if(event.getType() == Event.EVENT_TYPE.NEW_REQUEST){
                allRequests.add(event.getRequest());
            }
        }

        System.out.println("+-----------+------------------+----------------+---------------------+--------------+---------------+----------------+----------------------+--------------------+");
        StringBuilder data = new StringBuilder();
        double sumRefuseProb = 0;
        for (int i = 0; i < sourceCount; i++) {
            int requestCount = 0;
            int refusedCount = 0;
            for(Request request : allRequests){
                if(request.getSourceNumber() == i+1){
                    requestCount++;
                    if(request.isRefused()){
                        refusedCount++;
                    }
                }
            }

            double probOfRefuse = (double)refusedCount/requestCount;
            sumRefuseProb+=probOfRefuse;

            double totalBufferTime = 0;
            double totalDeviceTime = 0;
            for(Request request : allRequests){
                if(request.getSourceNumber() == i+1 & !request.isRefused()){
                    totalBufferTime += request.getOnDeviceTime() - request.getBufferedTime();
                    totalDeviceTime += request.getProcessedTime() - request.getOnDeviceTime();
                }
            }
            double avgBufferTime = totalBufferTime/(requestCount-refusedCount);
            double avgDeviceTime = totalDeviceTime/(requestCount-refusedCount);
            double avgTotalTime = avgBufferTime+avgDeviceTime;

            double sumBufferDispersion = 0;
            double sumDeviceDispersion = 0;
            for(Request request : allRequests){
                if(request.getSourceNumber() == i+1 & !request.isRefused()){
                    double requestBufferDispersion = pow(((request.getOnDeviceTime() - request.getBufferedTime()) - avgBufferTime), 2);
                    sumBufferDispersion += requestBufferDispersion;
                    double requestDeviceDispersion = pow(((request.getProcessedTime() - request.getOnDeviceTime()) - avgDeviceTime), 2);
                    sumDeviceDispersion += requestDeviceDispersion;
                }
            }
            double bufferTimeDispersion = sumBufferDispersion/(requestCount-refusedCount);
            double deviceTimeDispersion = sumDeviceDispersion/(requestCount-refusedCount);

            data.append(String.format("| %-6s %-2d | %-10s %-5d | %-7s %-5d | %-10s %-5.3f | %-4s %-7.3f | %-4s %-7.3f | %-5s %-7.3f | %-10s %-9.3f | %-10s %-7.3f |%n",
                    "Source", i, "generated:", requestCount,
                    "refused:", refusedCount, "P of refusal:", probOfRefuse,
                    "Тбп:", avgBufferTime, "Тобс:", avgDeviceTime, "Тпреб:", avgTotalTime,
                    "Disp Тбп:", bufferTimeDispersion, "Disp Тобс:", deviceTimeDispersion));
        }
        System.out.println(data + "+-----------+------------------+----------------+---------------------+--------------+---------------+----------------+----------------------+--------------------+");
        System.out.println("Average refuse prob: " + sumRefuseProb/sourceCount + "\n");


        System.out.println("+------------+------------+");
        StringBuilder deviceData = new StringBuilder();
        double totalTime = events.get(events.size()-1).eventTime() - events.get(0).eventTime(); // общее время считаем с момента приход первой заявки до ухода последней
        double totalLoad = 0;
        for(Device d : devices){
            deviceData.append(String.format("| %-6s %-2d | %-4s %.3f |%n", "Device ", devices.indexOf(d), "load", (d.getWorkingTime()/totalTime)));
            totalLoad += d.getWorkingTime()/totalTime;
        }
        System.out.println(deviceData + "+------------+------------+");
        System.out.println("Average workload: " + totalLoad/devices.size() + "\n");


    }

    public void addDevices(ArrayList<Device> devices){
        this.devices = devices;
    }
}
