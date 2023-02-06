package org.example;

import bftsmart.tom.ServiceProxy;
import bftsmart.tom.util.Storage;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.*;

import static org.example.testDataBuilder.*;

public class testClient {

    static int initId;
    static LinkedBlockingQueue<String> latencies;

    public static void main(String[] args) {
        // org.example.testClient initId operation ThreadNum OperationNum

        initId = Integer.parseInt(args[0]);
        latencies = new LinkedBlockingQueue<String>();
        int cmd = Integer.parseInt(args[1]);
        int threadNum = Integer.parseInt(args[2]);
        int ite = Integer.parseInt(args[3]);

        RunnableTestClient[] rtclients = new RunnableTestClient[threadNum];
        for (int i=0; i<threadNum; i++) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
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
        int numberOfOps;

        ServiceProxy clientProxy;

        public RunnableTestClient(int id, int cmd, int numberOfOps) {
            this.id = id;
            this.cmd = cmd;
            this.numberOfOps = numberOfOps;
            clientProxy = new ServiceProxy(id);
//            System.out.println("timeout value is " + clientProxy.getInvokeTimeout());
        }

        public void run() {
            if (cmd==1) PAPRun();
            if (cmd==2) PEPRun();


        }

        public void PAPRun() {
            System.out.println("runnable PAP client "+this.id+" created");
            int ind = 0;

            String value;
            String result;

            // for (; ind<numberOfOps/2; ind++) {
            //     value = "a new policy";
            //     try {
            //         result = update(value);
            //         if (ind%1000==0)
            //             System.out.println("update " + ind + " policy, PDP server return: " + result);
            //     } catch (Exception e) {
            //         System.err.println("update tx wrong!");
            //     }
            //     // try {
            //     //     Thread.sleep(10);
            //     // } catch (Exception e) {
            //     //     System.out.println("sleep error: "+ e);
            //     // }
            // }

            // Storage st = new Storage(numberOfOps / 2);
            System.out.println("start measuring...");

            for (; ind<numberOfOps; ind++) {
                // long last_send_instant = System.nanoTime();
                value = "a new policy";
                try {
                    result = update(value);
                    if (ind%10==0)
                        System.out.println("update " + ind + " policy, PDP server return: " + result);
                } catch (Exception e) {
                    System.err.println("update tx wrong!");
                }
                // long latency = System.nanoTime() - last_send_instant;
                // try {
                //     latencies.put(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                // } catch (InterruptedException ex) {
                //     ex.printStackTrace();
                // }
                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                    System.out.println("sleep error: "+ e);
                }

                // st.store(latency);
            }
            // if(id == initId) {
            //     System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (-10%) = " + st.getAverage(true) / 1000 + " us ");
            //     System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
            //     System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
            //     System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
            //     System.out.println(this.id + " // Maximum time for " + numberOfOps / 2 + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
            // }
            System.out.println("test client " + id + ": all "+ numberOfOps + " query txs has been sent, end...");


        }

        public void PEPRun() {
            System.out.println("runnable PEP client "+this.id+" created");
            int ind = 0;

            String result;

            for (; ind<numberOfOps/2; ind++) {
                Random ran = new Random(System.nanoTime() + this.id);
                int userid = ran.nextInt(USERNUM);
                int resourceid = ran.nextInt(RESOURCENUM);
                int amount = ran.nextInt(10);
                int totalamount = ran.nextInt(80);
                String kMarketRequest = createKMarketRequest("user"+userid, "resource"+resourceid,
                        amount, totalamount);
                try {
                    result = validate(kMarketRequest);
                    // System.out.println("["+ZonedDateTime.now() + "] client " + id +  " validate " + ind + " query, PDP server return: " + result);
                    if (ind%100==0)
                        System.out.println("client " + id +  " validate " + ind + " query, PDP server return: " + result);
                } catch (IOException e) {
                    System.err.println("query tx wrong! ioexeception");
                } catch (ClassNotFoundException e) {
                    System.err.println("query tx wrong! classnotfoundexeception");
                }

            }

            Storage st = new Storage(numberOfOps / 2);
            System.out.println("start measuring...");

            for (; ind<numberOfOps; ind++) {
                long last_send_instant = System.nanoTime();
                Random ran = new Random(System.nanoTime() + this.id);
                int userid = ran.nextInt(USERNUM);
                int resourceid = ran.nextInt(RESOURCENUM);
                int amount = ran.nextInt(10);
                int totalamount = ran.nextInt(80);
                String kMarketRequest = createKMarketRequest("user"+userid, "resource"+resourceid,
                        amount, totalamount);
                try {
                    result = validate(kMarketRequest);
                    // System.out.println("["+ZonedDateTime.now() + "] client " + id +  " validate " + ind + " query, PDP server return: " + result);
                    if (ind%100==0)
                        System.out.println("client " + id +  " validate " + ind + " query, PDP server return: " + result);
                } catch (IOException e) {
                    System.err.println("query tx wrong! ioexeception");
                } catch (ClassNotFoundException e) {
                    System.err.println("query tx wrong! classnotfoundexeception");
                }
                long latency = System.nanoTime() - last_send_instant;
                try {
                    latencies.put(id + "\t" + System.currentTimeMillis() + "\t" + latency + "\n");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                st.store(latency);
            }
            if(id == initId) {
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (-10%) = " + st.getAverage(true) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
                System.out.println(this.id + " // Maximum time for " + numberOfOps / 2 + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
            }
            System.out.println("test client " + id + ": all "+ numberOfOps + " query txs has been sent, end...");

        }


        public String update(String content) throws IOException, ClassNotFoundException {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            objOut.writeInt(0); // noop/add/delete a policy
            objOut.writeInt(0); // policy id
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
            objOut.writeInt(10);
            objOut.writeObject(content);
            objOut.flush();
            byteOut.flush();

            byte[] tmp = clientProxy.invokeOrdered(byteOut.toByteArray());
            if (tmp.length == 0) {
                System.out.println("invoke validating query, returns null");
                return null;
            }
            String reply = new String(tmp);
            return shortise(reply);
        }
    }


}
