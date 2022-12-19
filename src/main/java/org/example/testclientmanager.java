package org.example;

import bftsmart.tom.ServiceProxy;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.*;

public class testclientmanager {

    public static void main(String[] args) {
        int initId = Integer.parseInt(args[0]);
        int cmd = Integer.parseInt(args[1]);
        int threadNum = Integer.parseInt(args[2]);
        int ite = Integer.parseInt(args[3]);

        RunnableTestClient[] rtclients = new RunnableTestClient[threadNum];
        for (int i=0; i<threadNum; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("Launching runnable newclient " + (initId+i));
            rtclients[i] = new RunnableTestClient(initId+i, cmd, ite);
        }


        ExecutorService exec = Executors.newFixedThreadPool(rtclients.length);
        Collection<Future<?>> tasks = new LinkedList<>();

        for (RunnableTestClient c : rtclients) {
            tasks.add(exec.submit(c));
        }

        // wait for tasks completion
        for (Future<?> currTask : tasks) {
            try {
                currTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        exec.shutdown();

        System.out.println("All clients done.");


    }

    static class RunnableTestClient extends Thread {
        int id;
        int cmd;
        int maxIte;

        ServiceProxy clientProxy;

        public RunnableTestClient(int id, int cmd, int ite) {
            this.id = id;
            this.cmd = cmd;
            this.maxIte = ite;
            clientProxy = new ServiceProxy(id);
        }

        public void run() {
            System.out.println("runnable client "+this.id+" created");

            int ind = 0;
//            int policyId = ThreadLocalRandom.current().nextInt(0, 10000000 + 1);

            String value;
            String result = null;

            if (cmd==1) {
                while(true) {
                    ind += 1;
                    int rid = id * 1000 + ind;
                    value = testDataBuilder.giveMeOnePolicy("resource"+rid, 3);
                    try {
                        result = update(value);
                        System.out.println("update a policy, PDP server return: " + result);
                    } catch (Exception e) {
                        System.err.println("update tx wrong!");
                    }



                    if (ind >= maxIte) {
                        System.out.println("test client "+id+ ": all "+ maxIte + " update txs has been sent, end...");
                        break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println("sleep, but fail");
                    }
                }
            } else if (cmd==2) {
                while(true) {
                    ind += 1;
                    value = testDataBuilder.giveMeOneRequest("user1", "resource1", "action1");
                    try {
                        result = validate(value);
                        System.out.println("validate "+ind+" query, PDP server return: " + result);
                    } catch (IOException e) {
                        System.err.println("query tx wrong! ioexeception");
                    } catch (ClassNotFoundException e) {
                        System.err.println("query tx wrong! classnotfoundexeception");
                    }
//                    try {
//                        Thread.sleep(4);
//                    } catch (InterruptedException e) {
//                        System.out.println("sleep, but fail");
//                    }

                    if (ind >= maxIte) {
                        System.out.println("test client "+id+ "all "+ maxIte + " query txs has been sent, end...");
                        break;
                    }
                }
            }

        }



        public String update(String content) throws IOException, ClassNotFoundException {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            objOut.writeInt(1);
            objOut.writeObject(content);
            objOut.flush();
            byteOut.flush();

            byte[] tmp = clientProxy.invokeOrdered(byteOut.toByteArray());
            if (tmp.length == 0)
                return null;
            ByteArrayInputStream byteIn = new ByteArrayInputStream(tmp);
            ObjectInput objIn = new ObjectInputStream(byteIn);
            String reply = (String)objIn.readObject();

            return reply;
        }

        public String validate(String content) throws IOException, ClassNotFoundException {

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            objOut.writeInt(9);
            objOut.writeObject(content);
            objOut.flush();
            byteOut.flush();

            byte[] tmp = clientProxy.invokeOrdered(byteOut.toByteArray());

            if (tmp.length == 0) {
                System.out.println("invoke validating query, returns null");
                return null;
            }

            // qiwei, use String directly
//            ByteArrayInputStream byteIn = new ByteArrayInputStream(tmp);
//            ObjectInput objIn = new ObjectInputStream(byteIn);
//            String reply = (String)objIn.readObject();

            String reply = new String(tmp);


            return reply;
        }
    }

}
