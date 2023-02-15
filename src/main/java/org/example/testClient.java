package org.example;

import bftsmart.tom.ServiceProxy;
import bftsmart.tom.util.Storage;
import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;


import java.security.Signature;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


import static org.example.testDataBuilder.*;

public class testClient {



    private static int initId;
    private static LinkedBlockingQueue<String> latencies;





    public static void main(String[] args) {
        // org.example.testClient initId operation ThreadNum OperationNum
        latencies = new LinkedBlockingQueue<String>();

        initId = Integer.parseInt(args[0]);
        int threadNum = Integer.parseInt(args[1]);
        int ite = Integer.parseInt(args[2]);
        boolean signed = Boolean.parseBoolean(args[3]);
        int intv = Integer.parseInt(args[4]);
        String t = args[5];


        RunnableTestClient[] rtclients = new RunnableTestClient[threadNum];
        for (int i=0; i<threadNum; i++) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
            System.out.println("Launching runnable newclient " + ((initId+1)*1000+i));
            rtclients[i] = new RunnableTestClient((initId+1)*1000+i, ite, signed, intv, false);
        }

        ExecutorService exec=null;
        Collection<Future<?>> tasks = new LinkedList<>();


        if (t.equals("readwrite")) {
            RunnableTestClient rtpapclient = new RunnableTestClient(initId, ite, signed, intv, true);
            System.out.println("Launching runnable newclient " + (initId));
            rtclients[0].setLinkedPAP(rtpapclient);

            exec = Executors.newFixedThreadPool(rtclients.length+1);
            tasks.add(exec.submit(rtpapclient));
        } else if (t.equals("read")) {
            exec = Executors.newFixedThreadPool(rtclients.length);
        }
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



        List<Long> latencyres = new ArrayList<Long>();
        for (int i=1; i<threadNum; i++) { // skip the data from PAP client
            for (long x: rtclients[i].getLatencydata().getValues())
                latencyres.add(x);
        }
        long[] finaldata = new long[latencyres.size()];
        for(int i=0; i<latencyres.size(); i++) {
            finaldata[i] = latencyres.get(i);
        }
        double averagelatency = computeAverage(finaldata, true);


        System.out.println("All clients done. average latency is "+averagelatency + " ms");
        // System.out.println(Arrays.toString(latencyres.toArray()));

        exec.shutdown();
        System.out.println("All clients done.");

    }

    private static double computeAverage(long[] values, boolean percent) {
        Arrays.sort(values);
        int limit = 0;
        if (percent) {
            limit = values.length / 10;
        }

        long count = 0L;

        for(int i = limit; i < values.length - limit; ++i) {
            count += values[i];
        }

        return (double)count / (double)(values.length - 2 * limit);
    }




    static class RunnableTestClient extends Thread {
        int id;
        PrivateKey privateKey = null;
        //        int cmd;
        boolean signed;
        boolean testwrite;
        int numberOfOps;

        int displayInterval=10;

        Storage latencydata;

        ServiceProxy clientProxy;

        private final ReentrantLock canSendLock = new ReentrantLock();
        private final Condition cansendnow = canSendLock.newCondition();
        RunnableTestClient thelinkedpap;

        public RunnableTestClient(int id, int numberOfOps, boolean signed, int displayinterv, boolean testwrite) {

            this.id = id;
//            this.cmd = cmd;
            this.numberOfOps = numberOfOps;
            this.signed = signed;
            this.testwrite = testwrite;
            this.displayInterval = displayinterv;
            clientProxy = new ServiceProxy(id);
//            System.out.println("timeout value is " + clientProxy.getInvokeTimeout());
        }

        public void setLinkedPAP(RunnableTestClient rc) {
            thelinkedpap = rc;
        }

        private void canSend() {
            canSendLock.lock();
            cansendnow.signal();
            canSendLock.unlock();
        }

        public void run() {
            if (id<1000)
                PAPRun();
            else
                PEPRun();


        }

        public void PAPRun() {
            System.out.println("runnable PAP client "+this.id+" created");
            int ind = 0;

            String value;
            String result;

            System.out.println("start hearbeating...");
            while (true) {

                long last_send_instant = System.nanoTime();
                value = "a new policy";
                try {
                    result = update(value);
                    System.out.println("client "+ id + " update " + ind + " policy, PDP server return: " + result);
                } catch (Exception e) {
                    System.err.println("update tx wrong!");
                }
                ind++;
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    System.out.println("sleep error: "+ e);
//                }

                canSendLock.lock();
//                System.out.println("Waiting for send signal");
                cansendnow.awaitUninterruptibly();
//                System.out.println("got send signal");
                canSendLock.unlock();

//                st.store(latency);
            }
//            if(id == initId) {
//                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (-10%) = " + st.getAverage(true) / 1000 + " us ");
//                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (-10%) = " + st.getDP(true) / 1000 + " us ");
//                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (all samples) = " + st.getAverage(false) / 1000 + " us ");
//                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (all samples) = " + st.getDP(false) / 1000 + " us ");
//                System.out.println(this.id + " // Maximum time for " + numberOfOps / 2 + " executions (all samples) = " + st.getMax(false) / 1000 + " us ");
//            }
//            System.out.println("test client " + id + ": all "+ numberOfOps + " query txs has been sent, end...");


        }

        public void PEPRun() {



            System.out.println("runnable PEP client "+this.id+" created");



//            byte[] byte_prvkey = Base64.getDecoder().decode(privKey);
//            try {
//                KeyFactory factory = KeyFactory.getInstance("EC");
//                privateKey = (ECPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(byte_prvkey));
//            } catch (Exception e) {
//
//            }



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
                    if (ind%displayInterval==0)
                        System.out.println("client " + id +  " validate " + ind + " query, PDP server return: " + result);
                    if (thelinkedpap!=null && id==1000+initId && testwrite) {
                        thelinkedpap.canSend();
                        System.out.println("signaling my PAP!");
                    }
                } catch (IOException e) {
                    System.err.println("query tx wrong! ioexeception");
                } catch (ClassNotFoundException e) {
                    System.err.println("query tx wrong! classnotfoundexeception");
                }

            }

            latencydata = new Storage(numberOfOps / 2);
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
                    if (ind%displayInterval==0)
                        System.out.println("client " + id +  " validate " + ind + " query, PDP server return: " + result);
                    if (thelinkedpap!=null && id==1000+initId && testwrite) {
                        thelinkedpap.canSend();
                        System.out.println("signaling my PAP!");
                    }
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

                latencydata.store(latency);
            }
            if(id == (initId+1)*1000) {
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (-10%) = " + latencydata.getAverage(true) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (-10%) = " + latencydata.getDP(true) / 1000 + " us ");
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getAverage(false) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getDP(false) / 1000 + " us ");
                System.out.println(this.id + " // Maximum time for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getMax(false) / 1000 + " us ");
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


            byte[] request = content.getBytes();
            byte[] signature = new byte[0];
            if (this.signed) {
                try {
                    Signature ecdsaSign = TOMUtil.getSigEngine();
                    ecdsaSign.initSign(clientProxy.getViewManager().getStaticConf().getPrivateKey());
                    ecdsaSign.update(request);
                    signature = ecdsaSign.sign();
//                    System.out.println("sign succeed, signature length is "+signature.length+" bytes");
                } catch (Exception e) {
                    System.out.println("wrong in signing messages... "+e);
                }

            }

            ByteBuffer buffer = ByteBuffer.allocate(request.length+signature.length+Integer.BYTES*3);
            buffer.putInt(10);
            buffer.putInt(request.length);
            buffer.put(request);
            buffer.putInt(signature.length);
            buffer.put(signature);

            byte[] tmp = clientProxy.invokeOrdered(buffer.array());
            if (tmp.length == 0) {
                System.out.println("invoke validating query, returns null");
                return null;
            }
            String reply = new String(tmp);
            return shortise(reply);
        }

        public Storage getLatencydata() {
            return latencydata;
        }
    }




}
