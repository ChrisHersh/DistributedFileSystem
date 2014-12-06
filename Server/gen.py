import random

sizeOfRecords = 64
numOfRecords = 1
f = open("Records.txt", "w")
string = ""
for x in range(0, numOfRecords*sizeOfRecords):
	string += chr(random.randint(33, 122))
f.write(string + '\n')
 
