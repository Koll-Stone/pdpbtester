package org.example;

import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.PDPB.POrder;

import java.text.SimpleDateFormat;
import java.time.Instant;

public class testserver extends POrder {
    public testserver(int id, boolean signed) {
        xacmlExecutor xexecutor = new xacmlExecutor(signed);

        System.out.println("created xacml executor "+id);
        
        setReplicaId(id);
        SetPExecutor(xexecutor);
        setSysStartTime(System.currentTimeMillis());
        System.out.print("start point real time is: "+Instant.now().toString());

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
