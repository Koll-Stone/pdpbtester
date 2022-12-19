package org.example;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.w3c.dom.Document;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.updatablePolicyFinderModule;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class evaluationPrepare {

    private static Balana[] balana;
    private static int nWorkers = 1;
    private static ExecutorService parallelVerifier = Executors.newWorkStealingPool(nWorkers);



    public static void main(String[] args) {

        testBalana();
//        try {
//            testSig();
//        } catch (Exception e) {
//            System.out.println("error!");
//        }

    }

    public static void testBalana() {
        initProperty();
        updatablePolicyFinderModule[] upfmList = new updatablePolicyFinderModule[nWorkers];

        balana = new Balana[nWorkers];
        PDP[] pdpList = new PDP[nWorkers];
        for (int i=0; i<nWorkers; i++) {

            balana[i] = Balana.getInstance();
            upfmList[i] = new updatablePolicyFinderModule();
            Set<PolicyFinderModule> set1 = new HashSet<>();
            set1.add(upfmList[i]);
            balana[i].getPdpConfig().getPolicyFinder().setModules(set1);

            pdpList[i] = new PDP(new PDPConfig(null, balana[i].getPdpConfig().getPolicyFinder(), null, true));
        }


//        policyFinder.addModules(((PolicyFinderModule) upfm));



        System.out.println("Hello world!");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));


        List<List<Document>> newpolicies = new ArrayList<List<Document>>();
        for (int i=0; i<nWorkers; i++) {
            newpolicies.add(new ArrayList<Document>());
        }

//        String request = "<Request\n" +
//                "\txmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
//                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
//                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
//                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">action1</AttributeValue>\n" +
//                "\t\t</Attribute>\n" +
//                "\t</Attributes>\n" +
//                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
//                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
//                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">user1</AttributeValue>\n" +
//                "\t\t</Attribute>\n" +
//                "\t</Attributes>\n" +
//                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
//                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
//                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#anyURI\">resource1</AttributeValue>\n" +
//                "\t\t</Attribute>\n" +
//                "\t</Attributes>\n" +
//                "</Request>";





        List<Long> timecost = new ArrayList<Long>();
        Random ran = new Random();
        List<Integer> ResourceIds = new ArrayList<>();
        int RESOURCENUM= 10;
        for (int i=0; i<RESOURCENUM; i++) {
            ResourceIds.add(i);
        }
        Map<Integer, Integer> baselinemap = new HashMap<Integer, Integer>();
        baselinemap.put(0, 100);
        baselinemap.put(1, 100);

//            String onepolicy = testDataBuiler.giveMeOnePolicy("resource" + String.valueOf(i), 2);

//

        for (int i=0; i<1000; i++) {
            int userid = i/RESOURCENUM;
            Collections.shuffle(ResourceIds);
            String kmarketPolicy = createKMarketPolicy(""+i,"user"+userid, "resource"+ResourceIds.get(0),
                    "resource"+ResourceIds.get(1), "resource"+ResourceIds.get(2));
//            System.out.println("random resource ids: "+ResourceIds.get(0) + " " +ResourceIds.get(1)+" "+ResourceIds.get(2));
//            System.out.println("\n======================== XACML Policy ====================");
//            System.out.println(kmarketPolicy);
//            System.out.println("===========================================================");
            for (int k=0; k<nWorkers; k++) {
                newpolicies.get(k).add(testDataBuilder.toDocument(kmarketPolicy));
            }
        }



        for (int i=0; i<nWorkers; i++) {
            upfmList[i].loadPolicyBatchFromMemory(newpolicies.get(i));
        }

        for (int k=0; k<nWorkers; k++) {
            System.out.println("policy number : " + upfmList[k].showPolicies().size());
        }

        long start;
        long elapsedTime;
        String response;
        start = System.nanoTime();




        for (int i=0; i<1; i++) {
            int looptime = 1;
            final CountDownLatch latch = new CountDownLatch(looptime);

            start = System.nanoTime();
            for (int j=0; j<looptime; j++) {
                int userid = ran.nextInt(100);
                userid = 200;
                int resourceid = ran.nextInt(RESOURCENUM);
                int amount = ran.nextInt(20);
                int totalamount = ran.nextInt(1000);
                String kmarketrequest = createKMarketRequest("user"+userid, "resource"+resourceid,
                        amount, totalamount);
                System.out.println("request size is: "+kmarketrequest.length());
                parallelVerifier.submit(() -> {
                    int ind = (int) Thread.currentThread().getId() % nWorkers;
//                    System.out.println("thread id is " + ind);
                    String res = pdpList[ind].evaluate(kmarketrequest);
                    System.out.println("response size is: " + res.length());
                    System.out.println("response is\n"+res);
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println(e + "balana parallel execution fail");
            }

            elapsedTime = (System.nanoTime() - start)/1000000;
            timecost.add(elapsedTime);
            System.out.println("PDP evaluation 1000 requests costs " + (elapsedTime) + " ms");

//            System.out.println("\n======================== XACML Request ===================");
//            System.out.println(kmarketrequest);
//            System.out.println("===========================================================");
//            int tmp = (i%2==0)? 5: -5;
//            int x = baselinemap.get(0) + tmp;
//            int y = baselinemap.get(1) - tmp;
//            baselinemap.put(0, x);
//            baselinemap.put(1, y);
//
        }
        System.out.println("PDP evaluation costs: " + timecost);

//        start = System.nanoTime();
//        for (int i=0; i<1000; i++) {
//            response = pdp.evaluate(kmarketrequest3);
//            System.out.println("\n======================== XACML Response ===================");
//            System.out.println(response);
//            System.out.println("===========================================================");
//        }
//        elapsedTime = (System.nanoTime() - start)/1000000;
//        System.out.println("PDP evaluation costs " + (elapsedTime) + " ms");

    }



    private static void initProperty() {

        try{
            // using file based policy repository. so set the policy location as system property
            String policyLocation = (new File(".")).getCanonicalPath() + File.separator + "resources";
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);
            System.out.println("policy location:");
            System.out.println(policyLocation);
        } catch (IOException e) {
            System.err.println("Can not locate policy repository");
        }
        // create default instance of Balana
        // get a Balana instance
    }

    private static String createKMarketPolicy(String policyid, String subjectid, String resource1, String resource2, String resource3) {

        String kmarketPolicy = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" PolicyId=\"KmarketPolicy"+ policyid + "\"  RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides\" Version=\"1.0\">\n" +
                "   <Target>\n" +
                "      <AnyOf>\n" +
                "         <AllOf>\n" +
                "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + subjectid + "</AttributeValue>\n" +
                "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                "            </Match>\n" +
                "         </AllOf>\n" +
                "      </AnyOf>\n" +
                "   </Target>\n" +
                "   <Rule Effect=\"Deny\" RuleId=\"total-amount\">\n" +
                "      <Condition>\n" +
                "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than\">\n" +
                "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only\">\n" +
                "               <AttributeDesignator AttributeId=\"http://kmarket.com/id/totalAmount\" Category=\"http://kmarket.com/category\" DataType=\"http://www.w3.org/2001/XMLSchema#integer\" MustBePresent=\"true\"/>\n" +
                "            </Apply>\n" +
                "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">100</AttributeValue>\n" +
                "         </Apply>\n" +
                "      </Condition>\n" +
                "    <AdviceExpressions>\n" +
                "    <AdviceExpression AdviceId=\"deny-liquor-medicine-advice\" AppliesTo=\"Deny\">\n" +
                "    <AttributeAssignmentExpression AttributeId=\"urn:oasis:names:tc:xacml:2.0:example:attribute:text\">\n" +
                "\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">You are not allowed to do more than $100 purchase\n" +
                "    from KMarket on-line trading system</AttributeValue>\n" +
                "\t</AttributeAssignmentExpression>\n" +
                "    </AdviceExpression>\n" +
                "    </AdviceExpressions>\n" +
                "   </Rule>\n" +
                "   <Rule Effect=\"Deny\" RuleId=\"deny-liquor-medicine\">\n" +
                "   <Target>\n" +
                "      <AnyOf>\n" +
                "         <AllOf>\n" +
                "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource1 + "</AttributeValue>\n" +
                "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                "            </Match>\n" +
                "         </AllOf>\n" +
                "         <AllOf>\n" +
                "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource2 + "</AttributeValue>\n" +
                "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                "            </Match>\n" +
                "         </AllOf>\n" +
                "      </AnyOf>\n" +
                "   </Target>\n" +
                "  <AdviceExpressions>\n" +
                "    <AdviceExpression AdviceId=\"deny-liquor-medicine-advice\" AppliesTo=\"Deny\">\n" +
                "    <AttributeAssignmentExpression AttributeId=\"urn:oasis:names:tc:xacml:2.0:example:attribute:text\">\n" +
                "\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">You are not allowed to buy Liquor or Medicine\n" +
                "    from KMarket on-line trading system</AttributeValue>\n" +
                "\t</AttributeAssignmentExpression>\n" +
                "    </AdviceExpression>\n" +
                "    </AdviceExpressions>\n" +
                "   </Rule>\n" +
                "   <Rule Effect=\"Deny\" RuleId=\"max-drink-amount\">\n" +
                "   <Target>\n" +
                "      <AnyOf>\n" +
                "         <AllOf>\n" +
                "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource3 + "</AttributeValue>\n" +
                "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
                "            </Match>\n" +
                "         </AllOf>\n" +
                "      </AnyOf>\n" +
                "   </Target>\n" +
                "      <Condition>\n" +
                "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than\">\n" +
                "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only\">\n" +
                "               <AttributeDesignator AttributeId=\"http://kmarket.com/id/amount\" Category=\"http://kmarket.com/category\" DataType=\"http://www.w3.org/2001/XMLSchema#integer\" MustBePresent=\"true\"/>\n" +
                "            </Apply>\n" +
                "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">10</AttributeValue>\n" +
                "         </Apply>\n" +
                "      </Condition>\n" +
                "    <AdviceExpressions>\n" +
                "    <AdviceExpression AdviceId=\"max-drink-amount-advice\" AppliesTo=\"Deny\">\n" +
                "    <AttributeAssignmentExpression AttributeId=\"urn:oasis:names:tc:xacml:2.0:example:attribute:text\">\n" +
                "\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">You are not allowed to buy more tha 10 Drinks\n" +
                "    from KMarket on-line trading system</AttributeValue>\n" +
                "\t</AttributeAssignmentExpression>\n" +
                "    </AdviceExpression>\n" +
                "    </AdviceExpressions>\n" +
                "   </Rule>\n" +
                "    <Rule RuleId=\"permit-rule\" Effect=\"Permit\"/>    \n" +
                "</Policy>";
        return kmarketPolicy;
    }

    private static String createKMarketRequest(String user, String resource, int amount, int totalamount) {
        String kmarketrequest = "<Request\n" +
                "\txmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">\n" +
                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">action</AttributeValue>\n" +
                "\t\t</Attribute>\n" +
                "\t</Attributes>\n" +
                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">\n" +
                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + user + "</AttributeValue>\n" +
                "\t\t</Attribute>\n" +
                "\t</Attributes>\n" +
                "\t<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "\t\t<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">\n" +
                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource+ "</AttributeValue>\n" +
                "\t\t</Attribute>\n" +
                "\t</Attributes>\n" +
                "\t<Attributes Category=\"http://kmarket.com/category\">\n" +
                "\t\t<Attribute AttributeId=\"http://kmarket.com/id/amount\" IncludeInResult=\"false\">\n" +
                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + amount + "</AttributeValue>\n" +
                "\t\t</Attribute>\n" +
                "\t\t<Attribute AttributeId=\"http://kmarket.com/id/totalAmount\" IncludeInResult=\"false\">\n" +
                "\t\t\t<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + totalamount + "</AttributeValue>\n" +
                "\t\t</Attribute>\n" +
                "\t</Attributes>\n" +
                "</Request>";
        return kmarketrequest;
    }

//    public static void testSignature() throws NoSuchAlgorithmException {
//        Signature signature = Signature.getInstance("SHA256WithECDSA");
//        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA");
//        kpg.initialize(1024);
//
//    }

    public static void GetTimestamp(String info) {
        System.out.println(info + new Timestamp((new Date()).getTime()));
    }/*ww  w.ja va  2s  .  com*/

    public static byte[] GenerateSignature(String plaintext, KeyPair keys)
            throws SignatureException, UnsupportedEncodingException,
            InvalidKeyException, NoSuchAlgorithmException,
            NoSuchProviderException {
        Signature ecdsaSign = Signature
                .getInstance("SHA256withECDSA", "BC");
        ecdsaSign.initSign(keys.getPrivate());
        ecdsaSign.update(plaintext.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();
        System.out.println(signature.toString());
        return signature;
    }

    public static boolean ValidateSignature(String plaintext, KeyPair pair,
                                            byte[] signature) throws SignatureException,
            InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException, NoSuchProviderException {
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA",
                "BC");
        ecdsaVerify.initVerify(pair.getPublic());
        ecdsaVerify.update(plaintext.getBytes("UTF-8"));
        return ecdsaVerify.verify(signature);
    }


    public static KeyPair GenerateKeys() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        //  Other named curves can be found in http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
        ECParameterSpec ecSpec = ECNamedCurveTable
                .getParameterSpec("B-571");

        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");

        g.initialize(ecSpec, new SecureRandom());

        return g.generateKeyPair();
    }

    public static void testSig() throws Exception {

        long start;
        long elapsedTime;

        Security.addProvider(new BouncyCastleProvider());

        String plaintext = "Simple plain text";
        GetTimestamp("Key Generation started: ");
        KeyPair keys = GenerateKeys();
        //    System.out.println(keys.getPublic().toString());
        //    System.out.println(keys.getPrivate().toString());
        GetTimestamp("Key Generation ended: ");

        GetTimestamp("Signature Generation started: ");
        byte[] signature = GenerateSignature(plaintext, keys);
        GetTimestamp("Signature Generation ended: ");

        GetTimestamp("Validation started: ");

        for (int l=0; l<20; l++) {
            start  = System.nanoTime();

            int loopnum = 1000;
            final CountDownLatch latch = new CountDownLatch(loopnum);
            for (int i=0; i<loopnum; i++) {
                parallelVerifier.submit(() -> {
                    try {
                        boolean isValidated = ValidateSignature(plaintext, keys, signature);
//                        System.out.println("Result: " + isValidated);
                    } catch (Exception e) {

                    }
                    latch.countDown();
                });

            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println(e + "signature verification parallel execution fail");
            }
            elapsedTime = (System.nanoTime() - start)/1000000;
            System.out.println("signature evaluation costs " + (elapsedTime) + " ms");
        }


        GetTimestamp("Validation ended: ");

    }
}


// balana single thread time cost (ms)
// [18, 1561, 856, 897, 784, 994, 813, 720, 701, 703, 706, 698, 724, 698, 705, 692, 701, 716, 719, 701, 699, 703, 706, 706, 718, 719, 710, 715, 707, 721, 708, 701, 722, 718, 726, 714, 704, 710, 697, 714, 707, 715, 717, 749, 745, 743, 721, 709, 712, 731, 712, 724, 734, 710, 722, 708, 709, 739, 746, 716, 727, 751, 702, 724, 692, 701, 705, 712, 701, 727, 731, 778, 726, 697, 708, 697, 713, 731, 715, 782, 716, 699, 704, 927, 713, 694, 688, 704, 694, 690, 693, 692, 692, 720, 715, 699, 705, 708, 704, 728]
// balana 2-thread time cost (ms)
// [1086, 657, 595, 467, 518, 494, 453, 444, 433, 409, 426, 439, 417, 421, 393, 453, 410, 393, 436, 431, 425, 423, 407, 428, 409, 396, 404, 428, 435, 420, 418, 416, 422, 408, 401, 417, 406, 409, 428, 435, 423, 401, 435, 398, 436, 490, 423, 400, 406, 390, 392, 415, 401, 413, 429, 422, 425, 477, 414, 431, 409, 424, 407, 419, 450, 434, 413, 418, 422, 412, 490, 458, 406, 450, 428, 453, 425, 403, 423, 397, 431, 386, 443, 432, 415, 431, 461, 433, 421, 418, 389, 417, 446, 443, 430, 432, 420, 410, 432, 431]
// balana 4-thread time cost (ms)
// [780, 459, 448, 326, 342, 326, 313, 335, 282, 291, 277, 276, 283, 290, 318, 270, 275, 287, 276, 286, 286, 289, 280, 273, 283, 273, 271, 266, 267, 254, 274, 265, 298, 265, 255, 271, 277, 271, 271, 294, 283, 280, 287, 285, 283, 288, 286, 270, 278, 260, 279, 276, 271, 275, 278, 273, 278, 288, 278, 278, 288, 292, 283, 269, 267, 266, 271, 254, 276, 274, 273, 266, 293, 280, 279, 282, 278, 288, 277, 275, 281, 279, 272, 270, 281, 280, 272, 274, 280, 282, 277, 280, 275, 276, 273, 273, 281, 276, 278, 285]

// separated balana single thread time cost (ms)
// [1695, 817, 1008, 767, 760, 816, 748, 682, 662, 745, 731, 812, 750, 808, 680, 687, 674, 662, 697, 662, 678, 706, 674, 665, 691, 690, 682, 685, 668, 664, 674, 687, 665, 668, 684, 672, 666, 705, 744, 698, 696, 675, 668, 696, 667, 706, 708, 666, 672, 707, 672, 667, 732, 690, 682, 709, 680, 672, 707, 778, 729, 691, 663, 669, 691, 658, 669, 699, 690, 712, 705, 693, 666, 668, 677, 656, 657, 677, 671, 674, 710, 666, 665, 693, 671, 683, 721, 683, 680, 700, 671, 662, 671, 703, 674, 663, 681, 665, 661, 682]
// separated balana 2-thread time cost (ms)
// [1143, 603, 558, 649, 549, 501, 490, 417, 475, 407, 431, 428, 410, 427, 420, 427, 425, 442, 400, 420, 436, 466, 445, 426, 423, 446, 450, 413, 431, 429, 429, 433, 397, 408, 414, 446, 457, 400, 401, 432, 426, 439, 393, 419, 399, 419, 431, 428, 431, 414, 464, 469, 537, 425, 419, 437, 502, 499, 550, 507, 470, 444, 436, 499, 465, 466, 449, 452, 439, 491, 467, 492, 450, 696, 618, 530, 479, 492, 668, 415, 486, 487, 695, 614, 527, 563, 494, 495, 423, 456, 439, 477, 447, 428, 432, 422, 445, 402, 415, 423]
// [1252, 590, 496, 563, 478, 525, 426, 421, 454, 410, 448, 446, 408, 440, 429, 399, 444, 439, 447, 448, 439, 431, 422, 438, 429, 425, 428, 424, 463, 431, 454, 427, 455, 456, 413, 421, 437, 454, 428, 454, 468, 427, 441, 422, 439, 433, 435, 483, 434, 403, 484, 483, 537, 453, 463, 470, 543, 451, 403, 430, 446, 507, 604, 550, 486, 447, 434, 411, 399, 418, 735, 575, 570, 516, 535, 477, 456, 490, 490, 435, 437, 411, 425, 476, 439, 432, 433, 447, 758, 631, 469, 538, 747, 592, 454, 670, 421, 403, 432, 457]
// separated balana 4-thread time cost (ms)
// [856, 468, 607, 416, 417, 380, 304, 296, 292, 283, 277, 323, 288, 280, 275, 267, 254, 301, 294, 276, 292, 337, 338, 346, 342, 344, 307, 307, 330, 327, 346, 354, 273, 312, 263, 258, 261, 271, 279, 318, 285, 315, 272, 279, 262, 252, 290, 262, 277, 275, 277, 274, 273, 276, 322, 270, 276, 296, 272, 292, 310, 263, 325, 266, 269, 250, 279, 295, 271, 262, 284, 282, 263, 247, 259, 272, 294, 277, 288, 260, 255, 260, 256, 261, 250, 242, 277, 259, 254, 252, 248, 238, 250, 266, 270, 272, 267, 252, 261, 262]
// [1138, 441, 436, 327, 317, 300, 318, 286, 284, 275, 280, 269, 273, 262, 281, 283, 262, 282, 286, 263, 264, 282, 283, 297, 268, 272, 265, 270, 264, 277, 277, 289, 271, 274, 275, 269, 257, 264, 277, 286, 262, 262, 269, 276, 255, 266, 261, 286, 252, 266, 288, 268, 276, 274, 264, 289, 281, 271, 264, 289, 290, 280, 288, 285, 278, 283, 281, 309, 291, 270, 355, 333, 384, 416, 350, 375, 305, 282, 316, 286, 289, 269, 262, 305, 292, 303, 287, 269, 292, 354, 255, 289, 318, 328, 297, 292, 324, 287, 301, 275]