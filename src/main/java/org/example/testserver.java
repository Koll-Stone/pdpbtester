package org.example;


import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.PDPB.POrder;

public class testserver extends POrder {
    public testserver(int id, boolean signed) {
        xacmlExecutor xexecutor = new xacmlExecutor(signed);

        System.out.println("created xacml executor");

        setReplicaId(id);
        SetPExecutor(xexecutor);
        xexecutor.setPOrder(this);

        ServiceReplica sr = new ServiceReplica(id, this, this);
        xexecutor.setReplicaContext(sr.getReplicaContext());
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("try to sleep but failed");
        }
        setPDPBState(sr.getTomLayer().pdpbstate);
        setEchomanger(sr.getTomLayer().echoManager);
    }

    public static void main(String[] args) {
        new testserver(Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]));
    }

}
