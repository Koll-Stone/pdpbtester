package org.example;



import bftsmart.reconfiguration.util.ECDSAKeyLoader;
import bftsmart.tom.util.TOMUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;


import java.security.Signature;


import org.w3c.dom.Document;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.updatablePolicyFinderModule;

import static org.example.testDataBuilder.*;

public class solotest {

    int nWorkers=2;
    private final ExecutorService parallelVerifier = Executors.newWorkStealingPool(nWorkers);


    private Balana[] balanaList;
    private PDP[] pdpList;
    private updatablePolicyFinderModule[] upfmList;
    public List<Document> allPolicies = new ArrayList<Document>();

    PublicKey publicKey;
    PrivateKey privateKey;

    public solotest() {

    }

    public void performanceTest() throws Exception{


        System.out.println("solo test starts");
        ECDSAKeyLoader keyLoader = new ECDSAKeyLoader(0, "", true, "SHA256withECDSA");
        publicKey = keyLoader.loadPublicKey();
        privateKey = keyLoader.loadPrivateKey();

        System.out.println("initializing PDP");
        initProperty();
        balanaList = new Balana[nWorkers];
        pdpList = new PDP[nWorkers];
        upfmList = new updatablePolicyFinderModule[nWorkers];
        for (int i=0; i<nWorkers; i++) upfmList[i] = new updatablePolicyFinderModule();


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
                allPolicies.add(testDataBuilder.toDocument(kmarketPolicy));
            }
        }

        for (int i=0; i<nWorkers; i++) pdpList[i] = createThePDP(i);




        System.out.println("create requests********");
        boolean signed = true;
        byte[][] requests = createRequestBytes(signed, 1000);
        System.out.println("create requests********");


        System.out.println("\nstart performance testing...");
        List<Integer> res = new ArrayList<Integer>();
        for (int i=0; i<100; i++) {

            long start = System.currentTimeMillis();
            executeOpInParallel(requests, signed);
            long duration = System.currentTimeMillis()-start;
            if (i>=10 && i<90) {
                res.add((int) duration);
                System.out.println("performance testing costs " + duration + " ms");
            }
                
        }
        System.out.println("performance testing ends");
        System.out.print(Arrays.toString(res.toArray()));



    }


    public byte[][] createRequestBytes(boolean signed, int reqnum) {
        Random ran = new Random(System.nanoTime());
        int userid = ran.nextInt(USERNUM);
        int resourceid = ran.nextInt(RESOURCENUM);
        int amount = ran.nextInt(10);
        int totalamount = ran.nextInt(80);
        String kMarketRequest = createKMarketRequest("user"+userid, "resource"+resourceid,
                amount, totalamount);




        byte[][] requestbytes = new byte[reqnum][];
        for (int i=0; i<reqnum; i++) {
            byte[] request = kMarketRequest.getBytes();
            byte[] signature = new byte[0];
            if (signed) {
                try {
                    Signature ecdsaSign = TOMUtil.getSigEngine();
                    ecdsaSign.initSign(privateKey);
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

            requestbytes[i] = buffer.array();
        }

        return requestbytes;
    }


    public byte[][] executeOpInParallel(byte[][] commands, boolean signed) {

        System.out.println("validating "+commands.length+" requests in parallel");
        byte[][] replies = new byte[commands.length][];


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
        for (int i=0; i<commands.length; i++) {
            final int queryind = i;
            parallelVerifier.submit(() -> {
                // validate access request
                int tind = (int) Thread.currentThread().getId() % nWorkers;
                String result = pdpList[tind].evaluate(new String(queries[queryind])); // thread safe?
                replies[queryind] = result.getBytes();
//                 System.out.println("thread " + tind + " finished validating 1 request, the result is: " + shortise(result));

                // verify signature
                if (signed) {
                    try {
                        Signature ecdsaVerify = TOMUtil.getSigEngine();
                        ecdsaVerify.initVerify(publicKey);
                        ecdsaVerify.update(queries[queryind]);
                        if (!ecdsaVerify.verify(signatures[queryind])) {
                            System.out.println("Client sent invalid signature!");
                            System.exit(0);
                        } else {
//                             System.out.println("thread " + tind + " finished validating 1 request sig which is valid");
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


    public PDP createThePDP(int ind) {
        System.out.println("craete the pdp");
        Balana balana = Balana.getInstance();
        Set<PolicyFinderModule> set1 = new HashSet<>();
        set1.add(upfmList[ind]);
        balana.getPdpConfig().getPolicyFinder().setModules(set1);
        PDP pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
        upfmList[ind].loadPolicyBatchFromMemory(allPolicies);
        System.out.println("PDP "+ind+" has "+upfmList[ind].showPolicies().size()+" policies");
        return pdp;
    }
}
