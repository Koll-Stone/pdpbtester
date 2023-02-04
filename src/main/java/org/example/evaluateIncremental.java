package org.example;

import org.apache.xerces.impl.xs.SchemaSymbols;
import org.w3c.dom.Document;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.updatablePolicyFinderModule;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.example.testDataBuilder.*;

public class evaluateIncremental {
    static Balana balana;
    static PDP pdp;
    static List<Document> initialPolicies;
    static updatablePolicyFinderModule upfm;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        initProperty();
        upfm = new updatablePolicyFinderModule();
        initialPolicies = new ArrayList<Document>();

        List<Integer> ResourceIds = new ArrayList<>();
        for (int i=0; i<RESOURCENUM; i++) {
            ResourceIds.add(i);
        }

        int PolicyIndex = 0;
        Random rnd = new Random(0);
        for (int i=0; i<USERNUM; i++) {
            for (int j=0; j<POLICYEACHUSER; j++) {
                PolicyIndex++;
                Collections.shuffle(ResourceIds, rnd);
                int a1 = ResourceIds.get(0) % RESOURCENUM;
                int a2 = ResourceIds.get(1) % RESOURCENUM;
                int a3 = ResourceIds.get(2) % RESOURCENUM;
                String kmarketPolicy = testDataBuilder.createKMarketPolicy(""+PolicyIndex,"user"+i, "resource"+a1,
                        "resource"+a2, "resource"+a3);
                initialPolicies.add(testDataBuilder.toDocument(kmarketPolicy));
//                kmarketPolicy = testDataBuilder.createKMarketPolicy("-"+policyid,"user"+i, "resource"+a1,
//                        "resource"+a2, "resource"+a3);
//                allPolicies.add(testDataBuilder.toDocument(kmarketPolicy));
            }

        }



        long start = System.currentTimeMillis();
        balana = Balana.getInstance();
        upfm.loadPolicyBatchFromMemory(initialPolicies);
        Set<PolicyFinderModule> set1 = new HashSet<>();
        set1.add(upfm);
        balana.getPdpConfig().getPolicyFinder().setModules(set1);
        pdp = new PDP(new PDPConfig(null, balana.getPdpConfig().getPolicyFinder(), null, true));
        long duration=System.currentTimeMillis()-start;
        System.out.println("pdp creation time cost is " + duration + " ms, has "+ upfm.showPolicies().size() + " policies");

        for (int l=0; l<100; l++) {
            List<Document> newpolicies = new ArrayList<>();
            for (int i=0; i<10; i++) {
                String policyid = "policyid" + PolicyIndex;
                PolicyIndex++;
                Collections.shuffle(ResourceIds, rnd);
                int a1 = ResourceIds.get(0) % RESOURCENUM;
                int a2 = ResourceIds.get(1) % RESOURCENUM;
                int a3 = ResourceIds.get(2) % RESOURCENUM;
                String kmarketPolicy = testDataBuilder.createKMarketPolicy(policyid, "user" + i + USERNUM, "resource" + a1,
                        "resource" + a2, "resource" + a3);
                newpolicies.add(testDataBuilder.toDocument(kmarketPolicy));
            }
            start = System.currentTimeMillis();
            upfm.loadPolicyBatchFromMemory(newpolicies);
            duration=System.currentTimeMillis()-start;
            System.out.println("add new policy cost is " + duration + " ms, has "+ upfm.showPolicies().size() + " policies");
        }

    }

    private static void initProperty() {

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
}
