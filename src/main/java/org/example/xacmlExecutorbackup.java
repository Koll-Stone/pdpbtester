//package org.example;
//
//import bftsmart.tom.server.PDPB.PExecutor;
//import bftsmart.tom.server.PDPB.POrder;
//import org.w3c.dom.Document;
//import org.wso2.balana.Balana;
//import org.wso2.balana.PDP;
//import org.wso2.balana.PDPConfig;
//import org.wso2.balana.finder.PolicyFinderModule;
//import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
//import org.wso2.balana.finder.impl.updatablePolicyFinderModule;
//
//import javax.print.Doc;
//import javax.swing.plaf.basic.BasicLabelUI;
//import java.io.*;
//import java.net.URI;
//import java.util.*;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.locks.ReentrantLock;
//
//import static org.example.testDataBuilder.*;
//
//
//public class xacmlExecutorbackup extends PExecutor {
//
//    // todo, i can't use logger in this file, logger has no output to the console
//    private static Map<Integer, Balana> balanaList;
//    private static Map<Integer, PDP> pdpList;
//    private Map<Integer, updatablePolicyFinderModule> upfmList;
//    //    int nWorkers = Runtime.getRuntime().availableProcessors();
//    int nWorkers = 2;
//    private final ExecutorService parallelVerifier = Executors.newWorkStealingPool(nWorkers);
//
//    public POrder porder;
//
//    public List<Document> thePolicies = new ArrayList<Document>();
//
//    public xacmlExecutorbackup() {
//        System.out.println("initialize xacmlExecutor");
//        initProperty();
//
//        balanaList = new HashMap<Integer, Balana>();
//        pdpList = new HashMap<Integer, PDP>();
//        upfmList = new HashMap<Integer, updatablePolicyFinderModule>();
//
//        List<Integer> ResourceIds = new ArrayList<>();
//        for (int i=0; i<RESOURCENUM; i++) {
//            ResourceIds.add(i);
//        }
//        Random rnd = new Random(0);
//        int policyIndex = 0;
//        for (int i=0; i<USERNUM; i++) {
//            for (int j=0; j<POLICYEACHUSER; j++) {
//                policyIndex++;
//                Collections.shuffle(ResourceIds, rnd);
//                int a1 = ResourceIds.get(0) % RESOURCENUM;
//                int a2 = ResourceIds.get(1) % RESOURCENUM;
//                int a3 = ResourceIds.get(2) % RESOURCENUM;
//                String kmarketPolicy = testDataBuilder.createKMarketPolicy(""+policyIndex,"user"+i, "resource"+a1,
//                        "resource"+a2, "resource"+a3);
//                thePolicies.add(testDataBuilder.toDocument(kmarketPolicy));
//            }
//        }
//
//        System.out.println("xacmlExecutor initiazlize property successfully");
//    }
//
//    public void setPOrder(POrder pe) {
//        porder = pe;
//    }
//
//    @Override
//    public byte[] executeOp(int h, byte[] tx) {
//        // create PDP instance for this height if necessary
////        if (!balanaList.containsKey(h)) {
////            balanaList.put(h, Balana.getInstance());
////            updatablePolicyFinderModule upfm = new updatablePolicyFinderModule();
////            // inherit policy in proceeding upfm, begin
////            if (h>0) {
//////                for (Document du: upfmList.get(h-1).showPolicies()) {
////// todo, copy updatablePolicyFinderModule
//////                }
////            }
////            // inherit policy in proceeding upfm, begin
////            Set<PolicyFinderModule> hashSet1 = new HashSet<PolicyFinderModule>();
////            hashSet1.add(upfm);
////            upfmList.put(h, upfm);
////            balanaList.get(h).getPdpConfig().getPolicyFinder().setModules(hashSet1);
////            pdpList.put(h, new PDP(new PDPConfig(null, balanaList.get(h).getPdpConfig().getPolicyFinder(), null, true)));
////        }
//
//        if (!balanaList.containsKey(h)) {
//            long start = System.nanoTime();
//            pdpList.put(h, createThePDP());
//            long elapsedTime = (System.nanoTime() - start)/1000000;
////            System.out.println("pdp creation for height " + h + " costs "+ elapsedTime +" ms");
//        }
//
//
//        byte[] reply = null;
//        String content = null;
//        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tx);
//             ObjectInput objIn = new ObjectInputStream(byteIn);
//             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
//            int command = objIn.readInt();
//            content = (String)objIn.readObject();
////            if (command==1) {
////                // upload a policy
////                List<Document> newpolicies = new ArrayList<>();
////                newpolicies.add(testDataBuilder.toDocument(content));
////                upfmList.get(h).loadPolicyBatchFromMemory(newpolicies);
////                objOut.writeObject("upload policy finished!!!");
////            } else if (command==2) {
////                // delete a policy
////                upfmList.get(h).deletePolicy(URI.create(content));
////                objOut.writeObject("delete policyh finished!!!");
////            }
//            if (command==1) {
//                // do nothing
//                objOut.writeObject("upload policy finished");
//            } else {
//                System.out.println("****wrong**** it should be noop update!");
//            }
//            objOut.flush();
//            byteOut.flush();
//            reply = byteOut.toByteArray();
//        } catch (IOException | ClassNotFoundException e) {
//            System.out.println("Ocurred during PDPB Executor operation");
//        }
//        return reply;
//    }
//
//    /*
//    hs: hieghts
//    commands: xacml queries
//     */
//
//    @Override
//    public byte[][] executeOpInParallel(int[] hs, byte[][] commands) {
//        // check all dependency exists
//        for (int h:hs) {
////            if (!balanaList.containsKey(h) || !upfmList.containsKey(h) || !pdpList.containsKey(h)) {
//////                System.out.println("balanaList contains key "+ balanaList.containsKey(h));
//////                System.out.println("upfmList contains key "+upfmList.containsKey(h));
//////                System.out.println("pdpList contains key "+pdpList.containsKey(h));
////                throw new RuntimeException("Should never reach here! balana pdp for height "+h+" is not initialized yet");
////            }
//            if (!pdpList.containsKey(h)) {
//                throw new RuntimeException("Should never reach here! balana pdp for height "+h+" is not initialized yet");
//            }
//        }
//
//        byte[][] replies = new byte[hs.length][];
//        ReentrantLock replyLock = new ReentrantLock();
//
//        String[] queries = new String[hs.length];
//        for (int i=0; i<hs.length; i++) {
//            byte[] tx = commands[i];
////            if (tx==null) System.out.println("wrong!!! null tx!!");
//            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tx);
//                 ObjectInput objIn = new ObjectInputStream(byteIn);
//                 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();) {
//                int op = objIn.readInt();
//                if (op==2) { // it is indeed a query sent from pep clients
//                    String content = (String)objIn.readObject();
//                    queries[i] = content;
//                } else {
//                    System.out.println("****wrong**** it should be a query!");
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                System.out.println(e + "Ocurred during PDPB Executor operation");
//            }
//        }
//
//        final CountDownLatch latch = new CountDownLatch(hs.length);
////        System.out.println(hs.length+" requests to validate in parallel");
//        for (int i=0; i<hs.length; i++) {
//            final int ind = i;
//            parallelVerifier.submit(() -> {
//                try {
//                    int tind = (int) Thread.currentThread().getId() % nWorkers;
//                    String result = pdpList.get(hs[ind]).evaluate(queries[ind]); // thread safe?
////                    replyLock.lock();
//                    replies[ind] = result.getBytes();
////                    replyLock.unlock();
////                    System.out.println("thread " + tind + " finished validating 1 request, the result is: "
////                    + shortise(result));
//                }
//                catch (Exception e) {
//                    System.out.println("error in validating query");
//                }
//
//                latch.countDown();
//            });
//        }
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            System.out.println(e + "Ocurred during PDPB Executor operation");
//        }
//        return replies;
//    }
//
//    public void garbageCollect() {
//
//    }
//
//
//    private void initProperty() {
//
//        try{
//            // using file based policy repository. so set the policy location as system property
//            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
//            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
//            System.out.println("policy location:");
//            System.out.println(policyLocation);
//        } catch (IOException e) {
//            System.out.println("Can not locate policy repository");
//        }
//    }
//
//    static class task implements Runnable {
//        int taskId;
//
//        public task(int i) {
//            taskId = i;
//        }
//
//        public void run() {
//
//        }
//    }
//
//    public PDP createThePDP() {
//        Balana balana = Balana.getInstance();
//        updatablePolicyFinderModule upfm = new updatablePolicyFinderModule();
//        Set<PolicyFinderModule> set1 = new HashSet<>();
//        set1.add(upfm);
//        balana.getPdpConfig().getPolicyFinder().setModules(set1);
//        PDP pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
//
//
//        long start = System.nanoTime();
////        List<Document> newpolicies = new ArrayList<Document>();
////        for (Document p: thePolicies) {
////            newpolicies.add(p);
////        }
//        long elapsed = (System.nanoTime() - start)/1000000;
////        System.out.println("adding policy costs "+ elapsed +" ms");
//
//        //test deep copy
////        List<Document> copypolicies = new ArrayList<Document>();
////        for (Document dc: newpolicies) {
////            copypolicies.add(dc);
////        }
//        // test deep copy end
//
//
//        upfm.loadPolicyBatchFromMemory(thePolicies);
//        return pdp;
//    }
//
//    public PDP changeThePDP() {
//        Balana balana = Balana.getInstance();
//        updatablePolicyFinderModule upfm = new updatablePolicyFinderModule();
//        Set<PolicyFinderModule> set1 = new HashSet<>();
//        set1.add(upfm);
//        balana.getPdpConfig().getPolicyFinder().setModules(set1);
//        PDP pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
//
//
//        long start = System.nanoTime();
//        List<Document> newpolicies = new ArrayList<Document>();
//        for (Document p: thePolicies) {
//            newpolicies.add(p);
//        }
//        long elapsed = (System.nanoTime() - start)/1000000;
////        System.out.println("adding policy costs "+ elapsed +" ms");
//
//        //test deep copy
////        List<Document> copypolicies = new ArrayList<Document>();
////        for (Document dc: newpolicies) {
////            copypolicies.add(dc);
////        }
//        // test deep copy end
//
//
//        upfm.loadPolicyBatchFromMemory(newpolicies);
//        return pdp;
//    }
//
//}
