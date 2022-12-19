package org.example;

import bftsmart.tom.ServiceProxy;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class testclient {

    public testclient(int id) {
        ServiceProxy clientProxy = new ServiceProxy(id);

    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServiceProxy clientProxy = new ServiceProxy(Integer.parseInt(args[0]));

        int ind = 0;
        int maxIte = 20;
        String key, value, result;
        int policyId = ThreadLocalRandom.current().nextInt(0, 10000000 + 1);
        while(true) {
            ind += 1;
            key = "policyid" + policyId;
//            value = testDataBuilder.giveMeOnePolicy("resource"+policyId, 3);
//            result = update(clientProxy, value);
            value = testDataBuilder.giveMeOneRequest("user1", "resource1", "action1");
            result = validate(clientProxy, value);

            System.out.println("update a policy, PDP server return: " + result);

            if (ind>=maxIte) {
                System.out.println("all update completed");
                break;
            }

        }
    }





    public static String update(ServiceProxy clientProxy, String content) throws IOException, ClassNotFoundException {
        String reply = null;
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
        reply = (String)objIn.readObject();

        return reply;
    }

    public static String validate(ServiceProxy clientProxy, String content) throws IOException, ClassNotFoundException {
        String reply = null;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);
        objOut.writeInt(2);
        objOut.writeObject(content);
        objOut.flush();
        byteOut.flush();

        byte[] tmp = clientProxy.invokeOrdered(byteOut.toByteArray());
        if (tmp.length == 0)
            return null;
        ByteArrayInputStream byteIn = new ByteArrayInputStream(tmp);
        ObjectInput objIn = new ObjectInputStream(byteIn);
        reply = (String)objIn.readObject();

        return reply;
    }

}
