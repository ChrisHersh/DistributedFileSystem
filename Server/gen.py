import random

sizeOfRecords = 10
numOfRecords = 10
f = open("Records3.txt", "w")
string = ""
for _ in range(0, numOfRecords):
	for _ in range(0, sizeOfRecords)
            string += chr(random.randint(33, 122))
        string += '\n'
f.write(string)
 
