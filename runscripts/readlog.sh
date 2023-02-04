tail -6000 result/server0.java > result/simplelog0.java
tail -6000 result/server1.java > result/simplelog1.java
tail -6000 result/server2.java > result/simplelog2.java
tail -6000 result/server3.java > result/simplelog3.java

rm result/server0.java
rm result/server1.java
rm result/server2.java
rm result/server3.java

subl result/simplelog0.java
subl result/simplelog1.java
subl result/simplelog2.java
subl result/simplelog3.java
