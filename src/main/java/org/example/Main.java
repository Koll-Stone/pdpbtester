package org.example;

import org.wso2.balana.Balana;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");

        List<Integer> xlist = new ArrayList<Integer>();
        xlist.add(1);
        xlist.add(2);
        xlist.add(3);
        for (Integer x: xlist) {
            xlist.remove(x);
            System.out.println(x + "has been removed");
        }
        System.out.println("-------\n final list is" + xlist);
    }
}

// /usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/java-atk-wrapper.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jfr.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/management-agent.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/home/qiwei/xacmlProject/pdpbtester/pdpbTester/target/classes:/home/qiwei/.m2/repository/org/wso2/balana/org.wso2.balana/1.2.13-SNAPSHOT/org.wso2.balana-1.2.13-SNAPSHOT.jar:/home/qiwei/.m2/repository/org/wso2/balana/org.wso2.balana.utils/1.2.13-SNAPSHOT/org.wso2.balana.utils-1.2.13-SNAPSHOT.jar:/home/qiwei/.m2/repository/xerces/wso2/xercesImpl/2.8.1.wso2v2/xercesImpl-2.8.1.wso2v2.jar:/home/qiwei/.m2/repository/io/netty/netty-all/4.1.67.Final/netty-all-4.1.67.Final.jar:/home/qiwei/.m2/repository/ch/qos/logback/logback-classic/1.2.5/logback-classic-1.2.5.jar:/home/qiwei/.m2/repository/ch/qos/logback/logback-core/1.2.5/logback-core-1.2.5.jar:/home/qiwei/.m2/repository/org/slf4j/slf4j-api/1.7.31/slf4j-api-1.7.31.jar:/home/qiwei/.m2/repository/org/slf4j/jcl-over-slf4j/2.0.0-alpha7/jcl-over-slf4j-2.0.0-alpha7.jar:/home/qiwei/.m2/repository/org/bouncycastle/bcpkix-jdk15on/1.69/bcpkix-jdk15on-1.69.jar:/home/qiwei/.m2/repository/org/bouncycastle/bcprov-jdk15on/1.69/bcprov-jdk15on-1.69.jar:/home/qiwei/.m2/repository/org/bouncycastle/bcutil-jdk15on/1.69/bcutil-jdk15on-1.69.jar:/home/qiwei/.m2/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar:/home/qiwei/.m2/repository/org/ulisboa/bftsmart/1.0/bftsmart-1.0.jar