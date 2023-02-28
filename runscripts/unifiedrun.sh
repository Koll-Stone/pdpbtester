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

servernum=5


if [ $flag -eq 1 ]
then
    if [ $me -le $(($servernum-1)) ]
    then
        command="runscripts/myrun.sh org.example.testserver $me true"
    else
        client=$(($me-$servernum))
        echo "client is $client"
        pepinitid=$((($client+1)*1000))
        papnum=0
        # if [ $client -eq 0 ]
        # then
        #     papnum=20
        # fi
        command="runscripts/myrun.sh org.example.testClient 0 $papnum $pepinitid 220 300 true 100"
    fi   
    echo $command
    bash $command
fi