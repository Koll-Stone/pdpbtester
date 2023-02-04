echo "start..."

cd /home/qiwei/xacmlProject/mybftsmart/library

./gradlew installDist

cd /home/qiwei/xacmlProject/pdpbtester/pdpbTester

cp /home/qiwei/xacmlProject/mybftsmart/library/build/install/library/lib/BFT-SMaRt.jar lib

mvn install:install-file -Dfile=lib/BFT-SMaRt.jar -DgroupId=org.ulisboa -DartifactId=bftsmart -Dpackaging=jar -Dversion=1.0

#mvn dependency:build-classpath -Dmdep.outputFile=runscripts/cpcontent
#
#echo ":$(pwd)/target/classes" >> runscripts/cpcontent

echo "finished"