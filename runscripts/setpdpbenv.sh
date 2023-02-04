echo "######start..."

echo "######downloading library..."
cd /home/ubuntu/xacmlProject
rm -rf pdpbtester
git clone https://github.com/Koll-Stone/pdpbtester.git

echo "######setting environment..."
cd /home/ubuntu/xacmlProject/pdpbtester
mvn install:install-file -Dfile=lib/BFT-SMaRt.jar -DgroupId=org.ulisboa -DartifactId=bftsmart -Dpackaging=jar -Dversion=1.0
mvn dependency:build-classpath -Dmdep.outputFile=./runscripts/cpcontent
echo ":$(pwd)/target/classes" >> ./runscripts/cpcontent
mvn package

echo "finished"
