package org.example;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");

        solotest solotester = new solotest();
        try {
            solotester.performanceTest();
        } catch (Exception e) {
            System.out.println("exception!");
        }

    }
}