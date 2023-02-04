echo "start..."

cd /home/ubuntu/xacmlProject/mybftsmart/library

./gradlew installDist

cd /home/ubuntu/xacmlProject/pdpbtester/

cp /home/ubuntu/xacmlProject/mybftsmart/library/build/install/library/lib/BFT-SMaRt.jar lib

echo "finished"