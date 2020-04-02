#!/bin/sh
#************************************Author Information**************************************
				
#				     Danula Godagama 
#			    	 danula.godagama@uky.edu 
#			        University of Kentucky 
#				       04/02/2020 

#**************************************File Information***************************************
#				       Install.sh
#This shell script compile and install the SIS3153 Kmax driver components.
#This script should be run from this directory(SIS3153_Kmax_Driver).
#It assumes that the Kmax_Stuff folder is located in the home directory.
#User may need to change file locations according to the linux distribution. 
#libSIS3153.so need libusb package installed in your computer.
#JAVA-JDK version 1.8 or above to compile SIS3153 Java class libarary 
#*********************************************************************************************

# Compiling the SIS3153 Java class libarary 
javac SIS3153.java
jar -cmf m/home/mkovash/Kmax_Stuff/Extensions/Linux-amd64anifest.txt SIS3153.jar SIS3153.class 

# Compiling the libSIS3153.so native shared library 
gcc -Wall -fPIC -I./Include \-I/usr/include \-I/usr/lib/jvm/java-11-openjdk-amd64/include \-I/usr/lib/jvm/java-11-openjdk-amd64/include/linux -shared -o libSIS3153.so ./Src/* -L/lib/x86_64-linux-gnu -lusb

# installing libraries in proper locations 
rm ~/Kmax_Stuff/Extensions/Linux-amd64/SIS3153.jar ~/Kmax_Stuff/Extensions/Linux-amd64/libSIS3153.so
cp ./SIS3153.jar ~/Kmax_Stuff/Extensions/Linux-amd64
chmod +x ~/Kmax_Stuff/Extensions/Linux-amd64/SIS3153.jar
cp ./libSIS3153.so ~/Kmax_Stuff/Extensions/Linux-amd64
cp ./SIS3153_Controller.mdr ~/Kmax_Stuff/Extensions/VME.MDRL


