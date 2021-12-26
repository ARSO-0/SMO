package com.yurkov.Entity;

public record Event(com.yurkov.Entity.Event.EVENT_TYPE type, Request request, double eventTime,
                    SystemState systemState) {

    public enum EVENT_TYPE {NEW_REQUEST, REQUEST_BUFFERED, REQUEST_REFUSED, REQUEST_ON_DEVICE, REQUEST_PROCESSED}

    public EVENT_TYPE getType() {
        return type;
    }

    public Request getRequest() {
        return request;
    }

    public double getEventTime() {
        return eventTime;
    }

    public SystemState getSystemState() {
        return systemState;
    }
}
