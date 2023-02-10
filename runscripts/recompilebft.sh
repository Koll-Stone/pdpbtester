echo "######start..."

echo "######downloading bft smart..."
cd /home/ubuntu/xacmlProject/storage
git pull
# rm -rf storage
# git clone https://github.com/Koll-Stone/storage.git

echo "######compile bftsmart to maven..."
cp /home/ubuntu/xacmlProject/storage/BFT-SMaRt.jar /home/ubuntu/xacmlProject/pdpbtester/lib
cd /home/ubuntu/xacmlProject/pdpbtester
mvn clean install:install-file -Dfile=lib/BFT-SMaRt.jar -DgroupId=org.ulisboa -DartifactId=bftsmart -Dpackaging=jar -Dversion=1.0
mvn dependency:build-classpath -Dmdep.outputFile=runscripts/cpcontent
echo ":$(pwd)/target/classes" >> runscripts/cpcontent

echo "######finished..."