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



if [ $flag -eq 1 ]
then
    if [ $me -le 3 ]
    then
        command="runscripts/myrun.sh org.example.testserver $me false"
        echo $command
        bash $command
    else
        client=$(($me-4))
        # start=$(($(($client*600))+1001))
        command="runscripts/myrun.sh org.example.testClient $client 60 1001 false 20"
        echo $command
        bash $command
    fi   
fi

