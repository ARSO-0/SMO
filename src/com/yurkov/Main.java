package com.yurkov;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        double avgGenTime = 0;
        double genTimeDispersion = 0;
        double lambda = 0;
        int totalRequests = 0;
        int sourceCount = 0;
        int bufferCapacity = 0;
        int deviceCount = 0;

        try(Scanner scanner = new Scanner(new File("./" + "param.txt"))){
            totalRequests = Integer.parseInt(scanner.nextLine());
            sourceCount = Integer.parseInt(scanner.nextLine());
            avgGenTime = Double.parseDouble(scanner.nextLine());
            genTimeDispersion = Double.parseDouble(scanner.nextLine());
            bufferCapacity = Integer.parseInt(scanner.nextLine());
            deviceCount = Integer.parseInt(scanner.nextLine());
            lambda = Double.parseDouble(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SimulationService simulation = new SimulationService(totalRequests, sourceCount,
                    avgGenTime, genTimeDispersion,
                    bufferCapacity, deviceCount, lambda);
        simulation.printStatistics();
        Scanner scanner = new Scanner(System.in);
        System.out.println("See in step mode?(y/n)");
        if(Objects.equals(scanner.nextLine(), "y")){
            simulation.stepMode();
        }
    }
}
