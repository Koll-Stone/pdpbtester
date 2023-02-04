package org.example;

import bftsmart.tom.server.PDPB.PExecutor;
import bftsmart.tom.server.PDPB.POrder;
import org.w3c.dom.Document;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.updatablePolicyFinderModule;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.testDataBuilder.*;


public class xacmlExecutor extends PExecutor {

    // todo, i can't use logger in this file, logger has no output to the console
    private static Balana[] balanaList;
    private static PDP[] pdpList;
    private updatablePolicyFinderModule[] upfmList;


//    int nWorkers = Runtime.getRuntime().availableProcessors();
    int nWorkers = 2;
    private final ExecutorService parallelVerifier = Executors.newWorkStealingPool(nWorkers);

    public POrder porder;

    public List<Document> initialPolicies = new ArrayList<Document>();
    private Set<Integer> initialIds = new HashSet<Integer>();
    private Map<Integer, String> allPolicies = new HashMap<>();
    private Map<Integer, Set<Integer>> positiveUpdates = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, Set<Integer>> negetiveUpdates = new HashMap<Integer, Set<Integer>>();

    private int currentPDPHeight;

    public xacmlExecutor() {
        System.out.println("initialize xacmlExecutor");
        initProperty();

        balanaList = new Balana[nWorkers];
        pdpList = new PDP[nWorkers];
        upfmList = new updatablePolicyFinderModule[nWorkers];
        for (int i=0; i<nWorkers; i++) upfmList[i] = new updatablePolicyFinderModule();
        currentPDPHeight = 0;

        List<Integer> ResourceIds = new ArrayList<>();
        for (int i=0; i<RESOURCENUM; i++) {
            ResourceIds.add(i);
        }
        Random rnd = new Random(0);
        int policyIndex = 0;
        for (int i=0; i<USERNUM; i++) {
            for (int j=0; j<POLICYEACHUSER; j++) {
                policyIndex++;
                Collections.shuffle(ResourceIds, rnd);
                int a1 = ResourceIds.get(0) % RESOURCENUM;
                int a2 = ResourceIds.get(1) % RESOURCENUM;
                int a3 = ResourceIds.get(2) % RESOURCENUM;
                String kmarketPolicy = testDataBuilder.createKMarketPolicy(""+policyIndex,"user"+i, "resource"+a1,
                        "resource"+a2, "resource"+a3);
                initialIds.add(policyIndex);
                initialPolicies.add(testDataBuilder.toDocument(kmarketPolicy));
                allPolicies.put(policyIndex, kmarketPolicy);
            }
        }

        for (int i=0; i<nWorkers; i++) pdpList[i] = createThePDP(i);

        System.out.println("xacmlExecutor initiazlize property successfully");
    }

    public void setPOrder(POrder pe) {
        porder = pe;
    }

    @Override
    public byte[] executeOp(int h, byte[] tx) {
        // create PDP instance for this height if necessary
//        if (!balanaList.containsKey(h)) {
//            balanaList.put(h, Balana.getInstance());
//            updatablePolicyFinderModule upfm = new updatablePolicyFinderModule();
//            // inherit policy in proceeding upfm, begin
//            if (h>0) {
////                for (Document du: upfmList.get(h-1).showPolicies()) {
//// todo, copy updatablePolicyFinderModule
////                }
//            }
//            // inherit policy in proceeding upfm, begin
//            Set<PolicyFinderModule> hashSet1 = new HashSet<PolicyFinderModule>();
//            hashSet1.add(upfm);
//            upfmList.put(h, upfm);
//            balanaList.get(h).getPdpConfig().getPolicyFinder().setModules(hashSet1);
//            pdpList.put(h, new PDP(new PDPConfig(null, balanaList.get(h).getPdpConfig().getPolicyFinder(), null, true)));
//        }

//        if (currentPDPHead <h) currentPDPHead = h;

        byte[] reply = null;
        String content = null;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tx);
            ObjectInput objIn = new ObjectInputStream(byteIn);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            int command = objIn.readInt();
            int policyid = objIn.readInt();
            content = (String)objIn.readObject();
//            if (command==1) {
//                // upload a policy
//                List<Document> newpolicies = new ArrayList<>();
//                newpolicies.add(testDataBuilder.toDocument(content));
//                upfmList.get(h).loadPolicyBatchFromMemory(newpolicies);
//                objOut.writeObject("upload policy finished!!!");
//            } else if (command==2) {
//                // delete a policy
//                upfmList.get(h).deletePolicy(URI.create(content));
//                objOut.writeObject("delete policyh finished!!!");
//            }
            if (command==0) {
                // do nothing
                objOut.writeObject("noop policy update finished");
            } else if (command==1) {
                if (!positiveUpdates.containsKey(h)) positiveUpdates.put(h, new HashSet<Integer>());
                positiveUpdates.get(h).add(policyid);
                allPolicies.put(policyid, content);
            } else if (command==2) {
                if (!negetiveUpdates.containsKey(h)) negetiveUpdates.put(h, new HashSet<Integer>());
                negetiveUpdates.get(h).add(policyid);
            } else {
                System.out.println("****wrong**** it should be noop update!");
            }
            objOut.flush();
            byteOut.flush();
            reply = byteOut.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ocurred during PDPB Executor operation");
        }
        return reply;
    }

    /*
    hs: hieghts
    commands: xacml queries
     */

    @Override
    public byte[][] executeOpInParallel(int newhead, byte[][] commands) {

        // check all dependency exists
        // todo, change pdp to the height
        Set<Integer> IdtoAdd = new HashSet<Integer>();
        Set<Integer> IdtoDelete = new HashSet<Integer>();
        if (currentPDPHeight<newhead) {
            // apply all updates to pdp
            for (int i=currentPDPHeight+1; i<=newhead; i++) {
                if (positiveUpdates.containsKey(i)) IdtoAdd.addAll(positiveUpdates.get(i));
                if (negetiveUpdates.containsKey(i)) IdtoDelete.addAll(negetiveUpdates.get(i));
            }
            IdtoAdd.removeAll(IdtoDelete);
            IdtoDelete.removeAll(IdtoAdd);
            try {
                changeThePDP(IdtoAdd, IdtoDelete, newhead);
            } catch (URISyntaxException e) {
                System.out.println("error in changing PDP!");
            }
        } else if (currentPDPHeight>newhead) {
            // revoke all updates to pdp
            for (int i=newhead+1; i<=currentPDPHeight; i++) {
                if (positiveUpdates.containsKey(i)) IdtoDelete.addAll(positiveUpdates.get(i));
                if (negetiveUpdates.containsKey(i)) IdtoAdd.addAll(negetiveUpdates.get(i));
            }
            IdtoAdd.removeAll(IdtoDelete);
            IdtoDelete.removeAll(IdtoAdd);
            try {
                changeThePDP(IdtoAdd, IdtoDelete, newhead);
            } catch (URISyntaxException e) {
                System.out.println("error in changing PDP!");
            }
        }


        byte[][] replies = new byte[commands.length][];
        // ReentrantLock replyLock = new ReentrantLock();

        String[] queries = new String[commands.length];
        for (int i=0; i<commands.length; i++) {
            byte[] tx = commands[i];
//            if (tx==null) System.out.println("wrong!!! null tx!!");
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tx);
                 ObjectInput objIn = new ObjectInputStream(byteIn);
                 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();) {
                int op = objIn.readInt();
                if (op==10) { // it is indeed a query sent from pep clients
                    String content = (String)objIn.readObject();
                    queries[i] = content;
                } else {
                    System.out.println("****wrong**** it should be a query!");
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e + "Ocurred during PDPB Executor operation");
            }
        }

        final CountDownLatch latch = new CountDownLatch(commands.length);
//        System.out.println(hs.length+" requests to validate in parallel");
        for (int i=0; i<commands.length; i++) {
            final int queryind = i;
            parallelVerifier.submit(() -> {
                try {
                    int tind = (int) Thread.currentThread().getId() % nWorkers;
                    String result = pdpList[tind].evaluate(queries[queryind]); // thread safe?
//                    replyLock.lock();
                    replies[queryind] = result.getBytes();
//                    replyLock.unlock();
//                    System.out.println("thread " + tind + " finished validating 1 request, the result is: "
//                    + shortise(result));
                }
                catch (Exception e) {
                    System.out.println("error in validating query");
                }

                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            System.out.println(e + "Ocurred during PDPB Executor operation");
        }
        return replies;
    }

    public void garbageCollect() {

    }


    private void initProperty() {

        try{
            // using file based policy repository. so set the policy location as system property
            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
            System.out.println("policy location:");
            System.out.println(policyLocation);
        } catch (IOException e) {
            System.out.println("Can not locate policy repository");
        }
    }

    static class task implements Runnable {
        int taskId;

        public task(int i) {
            taskId = i;
        }

        public void run() {

        }
    }

    public PDP createThePDP(int ind) {
        System.out.println("craete the pdp");
        Balana balana = Balana.getInstance();
        Set<PolicyFinderModule> set1 = new HashSet<>();
        set1.add(upfmList[ind]);
        balana.getPdpConfig().getPolicyFinder().setModules(set1);
        PDP pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
        upfmList[ind].loadPolicyBatchFromMemory(initialPolicies);
        return pdp;
    }

    public void changeThePDP(Set<Integer> idstoadd, Set<Integer> idstodelete, int newhead) throws URISyntaxException {
        String action;
        if (currentPDPHeight<newhead) action = "update";
        else if (currentPDPHeight>newhead) action = "revoke";
        else action = "donothing";

        long start = System.currentTimeMillis();
        for (int i=0; i<nWorkers; i++) {
            for (Integer x: idstodelete) {
                upfmList[i].deletePolicy(new URI(x.toString()));
            }
        }

        List<Document> newpolicies = new ArrayList<Document>();
        for (Integer x: idstoadd) {
            newpolicies.add(testDataBuilder.toDocument(allPolicies.get(x)));
        }

        if (newpolicies.size()>0) {
            for (int i=0; i<nWorkers; i++) {
                upfmList[i].loadPolicyBatchFromMemory(newpolicies);
                System.out.println("pdp now has " + upfmList[i].showPolicies() + " policies");
            }
        }

        // long duration = System.currentTimeMillis() - start;
        // if (action=="revoke" || newhead-currentPDPHeight>=20) System.out.println("need " + action + " PDP from "+currentPDPHeight+ " to " + newhead +", time costs " + duration + " ms");

        currentPDPHeight = newhead;

    }

}
