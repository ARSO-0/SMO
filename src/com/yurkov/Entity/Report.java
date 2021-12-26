package com.yurkov.Entity;

import com.yurkov.Components.Device;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class Report {
    public boolean stepMode = true;
    private ArrayList<Event> events = new ArrayList<>();
    private ArrayList<RequestStats> requestStats = new ArrayList<>();
    private ArrayList<Device> devices = null;
    private final int sourceCount;
    private final int bufferCapacity;
    private final int deviceCount;
    private final int requestCount;


    public Report(int sourceCount, int bufferCapacity, int deviceCount, int requestCount) {
        this.sourceCount = sourceCount;
        this.bufferCapacity = bufferCapacity;
        this.deviceCount = deviceCount;
        this.requestCount = requestCount;
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
                String format = "| %-8s | %-10s | %-10s | %-8s | %-10s |";
                int tableSize = Math.max(Math.max(sourceCount, deviceCount), event.getSystemState().getBufferState().size());
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

                    if (deviceCount > i) {
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
        makeRequestStats();

        System.out.println("+-----------+------------------+----------------+---------------------+--------------+---------------+----------------+--------------------+--------------------+");
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < sourceCount; i++) {
            int requestCount = 0;
            int refusedCount = 0;
            for(RequestStats request : requestStats){
                if(request.sourceNumber == i+1){
                    requestCount++;
                    if(request.isRefused){
                        refusedCount++;
                    }
                }
            }

            double probOfRefuse = (double)refusedCount/requestCount;

            double totalBufferTime = 0;
            double totalDeviceTime = 0;
            for(RequestStats request : requestStats){
                if(request.sourceNumber == i+1 & !request.isRefused){
                    totalBufferTime += request.onDeviceTime - request.bufferedTime;
                    totalDeviceTime += request.processedTime - request.onDeviceTime;
                }
            }
            double avgBufferTime = totalBufferTime/(requestCount-refusedCount);
            double avgDeviceTime = totalDeviceTime/(requestCount-refusedCount);
            double avgTotalTime = avgBufferTime+avgDeviceTime;

            double sumBufferDispersion = 0;
            double sumDeviceDispersion = 0;
            for(RequestStats request : requestStats){
                if(request.sourceNumber == i+1 & !request.isRefused){
                    double requestBufferDispersion = pow(((request.onDeviceTime - request.bufferedTime) - avgBufferTime), 2);
                    sumBufferDispersion += requestBufferDispersion;
                    double requestDeviceDispersion = pow(((request.processedTime - request.onDeviceTime) - avgDeviceTime), 2);
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
        System.out.println(data + "+-----------+------------------+----------------+---------------------+--------------+---------------+----------------+--------------------+--------------------+\n");

        System.out.println("+------------+------------+");
        StringBuilder deviceData = new StringBuilder();
        double totalTime = events.get(events.size()-1).eventTime();
        for(Device d : devices){
            deviceData.append(String.format("| %-6s %-2d | %-4s %.3f |%n", "Device ", devices.indexOf(d), "load", (d.getWorkingTime()/totalTime)));
        }
        System.out.println(deviceData + "+------------+------------+\n");


    }

    private void makeRequestStats() {
        for (int i = 1; i <= requestCount; i++) {
            int requestNumber = -1;
            int sourceNumber = 0;
            double generationTime = 0;
            double bufferedTime = 0;
            double onDeviceTime = 0;
            double processedTime = 0;
            boolean isRefused = false;
            for(Event event : events){
                if(event.getRequest().getRequestNumber() == i){
                    if(requestNumber == -1){
                        requestNumber = event.getRequest().getRequestNumber();
                        sourceNumber = event.getRequest().getSourceNumber();
                    }
                    if(event.getType() == Event.EVENT_TYPE.NEW_REQUEST){
                        generationTime = event.getRequest().getGenerationTime();
                    }
                    if(event.getType() == Event.EVENT_TYPE.REQUEST_BUFFERED){
                        bufferedTime = event.getEventTime();
                    }
                    if(event.getType() == Event.EVENT_TYPE.REQUEST_REFUSED){
                        isRefused = true;
                    }
                    if(event.getType() == Event.EVENT_TYPE.REQUEST_ON_DEVICE){
                        onDeviceTime = event.getEventTime();
                    }
                    if(event.getType() == Event.EVENT_TYPE.REQUEST_PROCESSED){
                        processedTime = event.getEventTime();
                    }
                }
            }
            requestStats.add(new RequestStats(requestNumber, sourceNumber, generationTime, bufferedTime, onDeviceTime, processedTime, isRefused));
        }
    }

    public void addDevices(ArrayList<Device> devices){
        this.devices = devices;
    }

    private static class RequestStats{
        public int requestNumber;
        public int sourceNumber;
        public double generationTime;
        public double bufferedTime;
        public double onDeviceTime;
        public double processedTime;
        public boolean isRefused;

        public RequestStats(int requestNumber, int sourceNumber, double generationTime, double bufferedTime, double onDeviceTime, double processedTime, boolean isRefused) {
            this.requestNumber = requestNumber;
            this.sourceNumber = sourceNumber;
            this.generationTime = generationTime;
            this.bufferedTime = bufferedTime;
            this.onDeviceTime = onDeviceTime;
            this.processedTime = processedTime;
            this.isRefused = isRefused;
        }
    }

}
