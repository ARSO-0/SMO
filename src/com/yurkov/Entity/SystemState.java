package com.yurkov.Entity;

import com.yurkov.Components.Device;

import java.util.ArrayList;

public record SystemState(ArrayList<Request> bufferState,
                          ArrayList<Device> deviceState) {

    public ArrayList<Request> getBufferState() {
        return bufferState;
    }

    public ArrayList<Device> getDeviceState() {
        return deviceState;
    }
}
