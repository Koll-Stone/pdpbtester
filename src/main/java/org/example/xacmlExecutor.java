package org.example;

import bftsmart.tom.ReplicaContext;
import bftsmart.tom.server.PDPB.PExecutor;
import bftsmart.tom.server.PDPB.POrder;
import bftsmart.tom.util.TOMUtil;
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
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.testDataBuilder.*;


public class xacmlExecutor extends PExecutor {

    // todo, i can't use logger in this file, logger has no output to the console
    boolean signed;
    ReplicaContext replicaContext;

    private static Balana[] balanaList;
    private static PDP[] pdpList;
    private updatablePolicyFinderModule[] upfmList;


    //    int nWorkers = Runtime.getRuntime().availableProcessors();
    int nWorkers = 2;
    private final ExecutorService parallelVerifier = Executors.newWorkStealingPool(nWorkers);

    public POrder porder;

    public List<Document> checkpointheightPolicies = new ArrayList<Document>();
    private Set<Integer> currentheightIds = new TreeSet<Integer>();
    private Map<Integer, String> allPolicies = new HashMap<>();
    private Map<Integer, Set<Integer>> positiveUpdates = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, Set<Integer>> negetiveUpdates = new HashMap<Integer, Set<Integer>>();

    private int currentPDPHeight;

    PublicKey publicKey=null;


    public xacmlExecutor(boolean signed) {

        System.out.println("initialize xacmlExecutor");
        initProperty();

        this.signed = signed;
//        byte[] byte_pubkey = Base64.getDecoder().decode(pubKey);
//        try {
//            KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
//            publicKey = (ECPublicKey) factory.generatePublic(new X509EncodedKeySpec(byte_pubkey));
//        } catch (Exception e) {
//
//        }



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
        int stateleng = 0;
        for (int i=0; i<USERNUM; i++) {
            for (int j=0; j<POLICYEACHUSER; j++) {
                policyIndex++;
                Collections.shuffle(ResourceIds, rnd);
                int a1 = ResourceIds.get(0) % RESOURCENUM;
                int a2 = ResourceIds.get(1) % RESOURCENUM;
                int a3 = ResourceIds.get(2) % RESOURCENUM;
                String kmarketPolicy = testDataBuilder.createKMarketPolicy(""+policyIndex,"user"+i, "resource"+a1,
                        "resource"+a2, "resource"+a3);
//                System.out.println("kmarketpolicy length is "+ kmarketPolicy.length());
                stateleng += kmarketPolicy.length();
                currentheightIds.add(policyIndex);
//                System.out.println("adding policy: policy length "+xx.toString().length()+"\n"+xx.toString());
                checkpointheightPolicies.add(testDataBuilder.toDocument(kmarketPolicy));
                allPolicies.put(policyIndex, kmarketPolicy);
            }
        }

        for (int i=0; i<nWorkers; i++) pdpList[i] = createThePDP(i);

        long start = System.currentTimeMillis();
        byte[] snapshot = getSnapShot();
        long duration = System.currentTimeMillis() - start;
        System.out.println("snapshot costs "+duration+" ms, snapshot length is "+snapshot.length);

//        System.out.println("xacmlExecutor initiazlize property successfully, policylength is"+stateleng+
//                " snapshot is " + snapshot.toString().hashCode() + " snapshot length is "+snapshot.length);
    }

    public void setReplicaContext(ReplicaContext replicaContext) {this.replicaContext  = replicaContext;}

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
                objOut.writeObject("noop policy heartbeat finished");
            } else if (command==1) {
//                System.out.println("add a new policy: "+policyid);
                if (!positiveUpdates.containsKey(h)) positiveUpdates.put(h, new HashSet<Integer>());
                positiveUpdates.get(h).add(policyid);
                allPolicies.put(policyid, content);
                objOut.writeObject("add policy with id "+policyid+"! operation finished");
            } else if (command==2) {
//                System.out.println("remove an exising policy: "+policyid);
                if (!negetiveUpdates.containsKey(h)) negetiveUpdates.put(h, new HashSet<Integer>());
                negetiveUpdates.get(h).add(policyid);
                objOut.writeObject("remove policy with id "+policyid+"! operation finished");
            } else {
                System.out.println("****wrong**** don't know the update command!");
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
    newhead: hieght
    commands: xacml queries
     */

    @Override
    public byte[][] executeOpInParallel(int newhead, byte[][] commands) {


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
        ReentrantLock replyLock = new ReentrantLock();


        byte[][] queries = new byte[commands.length][];
        byte[][] signatures = new byte[commands.length][];
        for (int i=0; i<commands.length; i++) {
            byte[] tx = commands[i];
            ByteBuffer buffer = ByteBuffer.wrap(tx);
            int op = buffer.getInt();
            if (op==10) {
                int l = buffer.getInt();
                byte[] content = new byte[l];
                buffer.get(content);
                queries[i] = content;
                l = buffer.getInt();
                signatures[i] = new byte[l];
                buffer.get(signatures[i]);
            } else {
                System.out.println("****wrong**** it should be a query!");
            }
        }

        final CountDownLatch latch = new CountDownLatch(commands.length);
//        System.out.println(hs.length+" requests to validate in parallel");
        for (int i=0; i<commands.length; i++) {
            final int queryind = i;
            parallelVerifier.submit(() -> {
                // validate access request
                int tind = (int) Thread.currentThread().getId() % nWorkers;
                String result = pdpList[tind].evaluate(new String(queries[queryind])); // thread safe?
                replies[queryind] = result.getBytes();
//                System.out.println("thread " + tind + " finished validating 1 request, the result is: " + shortise(result));

                // verify signature
                if (this.signed) {
                    try {
                        Signature ecdsaVerify = TOMUtil.getSigEngine();
                        ecdsaVerify.initVerify(replicaContext.getStaticConfiguration().getPublicKey());
                        ecdsaVerify.update(queries[queryind]);
                        if (!ecdsaVerify.verify(signatures[queryind])) {
                            System.out.println("Client sent invalid signature!");
                            System.exit(0);
                        } else {
//                            System.out.println("thread " + tind + " finished validating 1 request sig which is valid");
                        }
                    } catch (Exception e) {
                        System.out.println("error in validating query " + e);
                    }
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

    @Override
    public byte[] getSnapShot() {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        for (int x: currentheightIds) {
            try {
//                System.out.println("snapshot: writing policy length "+ allPolicies.get(x).getBytes().length);
                outputStream.write(allPolicies.get(x).getBytes());
            } catch (IOException e) {
                System.out.println("write state log wrong " + e);
            }
        }

        // todo, garbage collect positveupdate/negativeupdate



//        for (Document policy: checkpointheightPolicies) {
//            try {
//                System.out.println("snapshot: writing policy length "+ policy.toString().length());
//                outputStream.write(policy.toString().getBytes());
//            } catch (IOException e) {
//                System.out.println("write state log wrong " + e);
//            }
//        }
        return outputStream.toByteArray();
    }

    @Override
    public void executorstateGC() {

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

    public PDP createThePDP(int ind) {
        System.out.println("create the pdp");
        Balana balana = Balana.getInstance();
        Set<PolicyFinderModule> set1 = new HashSet<>();
        set1.add(upfmList[ind]);
        balana.getPdpConfig().getPolicyFinder().setModules(set1);
        PDP pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
        upfmList[ind].loadPolicyBatchFromMemory(checkpointheightPolicies);
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
                String s = "KmarketPolicy" + x;
                upfmList[i].deletePolicy(new URI(s));
//                System.out.println("changing PDP: remove policy with id "+x);
                currentheightIds.remove(x);
            }
        }

        List<Document> newpolicies = new ArrayList<Document>();
        for (Integer x: idstoadd) {
            newpolicies.add(testDataBuilder.toDocument(allPolicies.get(x)));
            currentheightIds.add(x);
        }

        if (newpolicies.size()>0) {
            for (int i=0; i<nWorkers; i++) {
                upfmList[i].loadPolicyBatchFromMemory(newpolicies);
//                System.out.println("pdp now has " + upfmList[i].showPolicies().size() + " policies");
            }
        }


        long duration = System.currentTimeMillis() - start;
        if (action=="revoke" || newhead-currentPDPHeight>=2)
        System.out.println("need " + action + " PDP from "+currentPDPHeight+ " to " + newhead +
                ", time costs " + duration + " ms, pdp now has " + upfmList[0].showPolicies().size() + " policies");
        if (newhead%100==0)
            System.out.println("pdp at height "+newhead+ " now has " + upfmList[0].showPolicies().size() + " policies");

        currentPDPHeight = newhead;

    }

}
