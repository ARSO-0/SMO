package com.yurkov.Components;

import com.yurkov.Entity.Request;

import java.util.*;

public class Buffer {

    private ArrayList<Request> array = new ArrayList<>();
    private final int capacity;
    private int size = 0;

    public Buffer(int capacity){
        this.capacity = capacity;
    }

    public boolean isAvailable(){
        return capacity > size;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public void addRequest(Request request){
        array.add(request);
        size++;
    }

    public Request fetchRequest(){
        if(isEmpty()){
            return null;
        }
        Request request = array.stream().max(new Request.RequestComparator()).orElse(null);
        array.remove(request);
        size--;
        return request;

    }

    public ArrayList<Request> getArray() {
        return array;
    }
}
