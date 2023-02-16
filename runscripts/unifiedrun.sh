#!bin/bash
echo "######unified running starts..."

cd /home/ubuntu/xacmlProject/pdpbtester

echo "######running code..."
myip=$(hostname --ip-address)
echo "$myip"
input="/home/ubuntu/xacmlProject/pdpbtester/config/ips"

ind=0
me=-1
flag=-1
while read line; do    
    if [[ $line == $myip ]]
    then
        echo "yes me is $ind"
        flag=1
        me=$ind
    fi
    ((ind++))
done < $input

servernum=4


if [ $flag -eq 1 ]
then
    if [ $me -le $(($servernum-1)) ]
    then
        command="runscripts/myrun.sh org.example.testserver $me true"
        echo $command
        bash $command
    else
        client=$(($me-$servernum))
        # start=$(($(($client*1000))+1001))
        command="runscripts/myrun.sh org.example.testClient $client 90 670 true 100 read"
        echo $command
        bash $command
    fi   
fi