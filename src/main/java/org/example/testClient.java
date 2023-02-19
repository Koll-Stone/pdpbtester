package org.example;

import bftsmart.tom.ServiceProxy;
import bftsmart.tom.util.Storage;
import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
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



    private static int papInitId;
    private static int pepInitId;
    private static LinkedBlockingQueue<String> latencies;





    public static void main(String[] args) {
        // org.example.testClient initId operation ThreadNum OperationNum
        latencies = new LinkedBlockingQueue<String>();

        papInitId = Integer.parseInt(args[0]);
        int papNum = Integer.parseInt(args[1]);
        pepInitId = Integer.parseInt(args[2]);
        int pepNum = Integer.parseInt(args[3]);
        int pepIteration = Integer.parseInt(args[4]);
        boolean signed = Boolean.parseBoolean(args[5]);
        int verboseInterval = Integer.parseInt(args[6]);
//        String t = args[6];


        RunnableTestClient[] rtclients = new RunnableTestClient[pepNum+papNum];
        // create pep instance
        for (int i=0; i<pepNum; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("Launching runnable PEP " + (pepInitId+i));
            rtclients[i] = new RunnableTestClient(pepInitId+i, 0, pepIteration, signed, verboseInterval, false);
        }
        // create pap instance
        for (int i=0; i<papNum; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            rtclients[pepNum+i] = new RunnableTestClient(papInitId+i, papNum, pepIteration, signed, verboseInterval, true);
            System.out.println("Launching runnable PAP " + i);
        }


        ExecutorService exec=Executors.newFixedThreadPool(papNum+ pepNum);
        List<Future<?>> tasks = new LinkedList<>(); // pepclents then pap clients
        for (RunnableTestClient rt:rtclients) {
            tasks.add(exec.submit(rt));
        }
//        for (int j=0; j<pepNum; j++) {
//            tasks.add(exec.submit(rtclients[j]));
//        }



        // wait for PEP tasks completion
        for (int j=0; j<pepNum; j++) {
            Future<?> currTask = tasks.get(j);
            try {
                currTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("All PEP clients done, exiting main thread...");
//        for (Future<?> currTask : tasks) {
//
//            try {
//                currTask.get();
//            } catch (InterruptedException | ExecutionException ex) {
//                ex.printStackTrace();
//            }
//        }



        List<Long> latencyres = new ArrayList<Long>();
        for (int i=0; i<pepNum; i++) { // skip the data from PAP client
            for (long x: rtclients[i].getLatencydata().getValues())
                latencyres.add(x);
        }
        long[] finaldata = new long[latencyres.size()];
        for(int i=0; i<latencyres.size(); i++) {
            finaldata[i] = latencyres.get(i);
        }
        double averagelatency = computeAverage(finaldata, true);


        System.out.println("All PEP clients done. average latency is "+averagelatency / 1000000 + " ms");
        // System.out.println(Arrays.toString(latencyres.toArray()));

        exec.shutdown();


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
        int totalpapnum;
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

        public RunnableTestClient(int id, int totalpapnum, int numberOfOps, boolean signed, int displayinterv, boolean testwrite) {

            this.id = id;
            this.totalpapnum = totalpapnum;
            this.numberOfOps = numberOfOps;
            this.signed = signed;
            this.testwrite = testwrite;
            this.displayInterval = displayinterv;
            clientProxy = new ServiceProxy(id);
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
            System.out.println("run() called, id="+id);
            if (id<1000)
                PAPRun(this.totalpapnum);
            else
                PEPRun();


        }

        public void PAPRun(int totalpapnum) {
            System.out.println("runnable PAP client "+this.id+" created");

            // prepare data related to policy update
            int currentcmd = 0;
            int policyid = 0;

            List<Integer> ResourceIds = new ArrayList<>();
            for (int i=0; i<RESOURCENUM; i++) {
                ResourceIds.add(i);
            }

            Queue<Integer> policyIdQueue = new LinkedList<Integer>();
            int policyeachpaptackle=USERNUM*POLICYEACHUSER / totalpapnum;
            int policyofme = this.id * policyeachpaptackle;
            for (int j=policyofme; j<policyofme+policyeachpaptackle; j++)
                policyIdQueue.offer(j); // assign intial policies to each pap for them to use in remove operation
            policyofme = 10000*(this.id+1);

            // start running!
            int ind = 0;
            String value="";
            String result;

            Random rand = new Random(this.id);

            System.out.println("start hearbeating...");
            while (true) {

                long last_send_instant = System.nanoTime();
                if (currentcmd+1==1) {
//                    System.out.println("pap client "+id+" operation "+ind+": add!");

                    // pick a random target and generate policy
                    int userid = rand.nextInt(USERNUM);
                    Collections.shuffle(ResourceIds);
                    int a1 = ResourceIds.get(0) % RESOURCENUM;
                    int a2 = ResourceIds.get(1) % RESOURCENUM;
                    int a3 = ResourceIds.get(2) % RESOURCENUM;
//                    generate policy
                    value = testDataBuilder.createKMarketPolicy(""+policyofme,"user"+userid, "resource"+a1,
                            "resource"+a2, "resource"+a3);
//                    save it
                    policyIdQueue.offer(policyofme);
                    policyid = policyofme;
                    policyofme++;

                } else if (currentcmd+1==2) {
//                    System.out.println("pap client "+id+" operation "+ind+": remove!");
                    value = "remove it";
                    policyid =  policyIdQueue.poll();
                }

                try {
//                    System.out.println("send pap operation");
                    result = update(currentcmd+1, policyid, value);
                    // cmd=0: noop
                    // cmd=1: add a new policy with id;
                    // cmd=2: remove an existing policy with id;
                    if (ind%this.displayInterval==0)
                        System.out.println("client "+ id + " did " + ind + " updates! latest reply: " + result);
                } catch (Exception e) {
                    System.err.println("update tx wrong!");
                }
                ind++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("sleep error: "+ e);
                }

                currentcmd = (currentcmd+1)%2;

//                canSendLock.lock();
//                System.out.println("Waiting for send signal");
//                cansendnow.awaitUninterruptibly();
//                System.out.println("got send signal");
//                canSendLock.unlock();

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
                    if (thelinkedpap!=null && id==1000+ papInitId && testwrite) {
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
                    if (thelinkedpap!=null && id==1000+ papInitId && testwrite) {
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
            if(id == (papInitId +1)*1000) {
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (-10%) = " + latencydata.getAverage(true) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (-10%) = " + latencydata.getDP(true) / 1000 + " us ");
                System.out.println(this.id + " // Average time for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getAverage(false) / 1000 + " us ");
                System.out.println(this.id + " // Standard desviation for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getDP(false) / 1000 + " us ");
                System.out.println(this.id + " // Maximum time for " + numberOfOps / 2 + " executions (all samples) = " + latencydata.getMax(false) / 1000 + " us ");
            }
            System.out.println("test client " + id + ": all "+ numberOfOps + " query txs has been sent, end...");

        }


        public String update(int cmd, int policyid, String content) throws IOException, ClassNotFoundException {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);
            objOut.writeInt(cmd); // noop/add/delete a policy
            objOut.writeInt(policyid); // policy id
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
