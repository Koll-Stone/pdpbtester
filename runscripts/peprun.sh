gnome-terminal -- /usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java -Djava.security.properties="./config/java.security" -Dlogback.configurationFile="config/logback.xml" -Dfile.encoding=UTF-8 -classpath $(cat "./runscripts/cpcontent") org.example.testclientmanager 1000 2 1 100 &