rm topoutput.txt
while true;
    do top -b -n 1 | grep -A 3 'CPU' >> topoutput.txt
    sleep 1
    echo "logged!"
done