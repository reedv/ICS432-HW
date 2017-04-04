Reed Villanueva
ics432
hw8

To compile everything:
	javac -cp .:Filters.jar *.java

How to test weird filter:
	javac RGB.java WeirdFilter.java WeirdFilterExample.java
	java WeirdFilterExample <path to image>
Should then create the resulting image in the current directory under the name weird_image.jpg.