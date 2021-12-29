package com.yurkov.Managers;

import com.yurkov.Components.Buffer;
import com.yurkov.Entity.Event;
import com.yurkov.Entity.Report;
import com.yurkov.Entity.Request;
import com.yurkov.SimulationService;

import java.util.ArrayList;

public class BufferManager {

    private final Buffer buffer;
    private final Report report;
    private final SimulationService ownerService;

    public BufferManager(int bufferCapacity, Report report, SimulationService ownerService){
        this.buffer = new Buffer(bufferCapacity);
        this.report = report;
        this.ownerService = ownerService;
    }

    public void enqueueRequest(Request request, double currentTime){
        if(buffer.isAvailable()){  // Д1ОЗ3 - на свободное место
            buffer.addRequest(request);
            request.setBufferedTime(currentTime);
            report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_BUFFERED, request, currentTime, ownerService.getCurrentSystemState()));
        } else {
            request.setRefused(true);
            report.addEvent(new Event(Event.EVENT_TYPE.REQUEST_REFUSED, request, currentTime, ownerService.getCurrentSystemState()));  // Д1ОО5 — вновь пришедший запрос
        }
    }

    public Request fetchRequest(){
        // Д2Б4 – приоритет по номеру источника, по одной заявке(среди заявок одного приоритета имеет смысл выбирать последнюю поступившую
        return buffer.fetchRequest();
    }

    public boolean isEmpty(){
        return buffer.isEmpty();
    }

    public ArrayList<Request> getState(){
        return new ArrayList<>(buffer.getArray());
    }
}
