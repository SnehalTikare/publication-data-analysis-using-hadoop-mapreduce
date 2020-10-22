# Homework 2
### Description: The objective of this project is to design and implement an instance of the map/reduce computational model on DBLP dataset

## Overview
In this project, we will create Map Reduce programs for parallel processing of the [publically available DBLP dataset](https://dblp.uni-trier.de) that contains entries for various publications at many different venues (e.g., conferences and journals). 

## Instructions
Below are the instructions to run the project

### Prerequisities
Below are the prerequisities required to run the program
```
jdk-1.8.0_265
Scala v2.13.3 
Hadoop v3.3.0
Simple build tool (SBT) v1.1.2
```
### Steps to setup the environment for execution 
- To setup the Hadoop environment on your system, you need to install Apache Hadoop or alternatively use Virtual Box to install Hortonworks Sandbox,which consists of pre-configured Apache libraries 
- Once the environment is setup, run the below command to start the Hadoop cluster
```
$ start-all.sh
```
- Create a input folder in HDFS directory
```
hdfs dfs -mkdir /input
```
- Place the input file dblp.xml in the input directory
```
hdfs dfs -put dblp.xml /input
```
### Next, steps to run the map-reduce program in distributed environment using Hadoop
- Clone the repository to your local system using the command below. Or download the zip folder;
```
$ git clone https://SnehalTikare@bitbucket.org/cs441-fall2020/snehal_tikare_hw2.git
```
- Navigate to the directory of the project;
- Use the below command to create a JAR file of the project;
```
$ sbt clean compile assembly
```
After running the above command, a jar file is created under the path target/scala-2.13/

- Traverse to the folder where the JAR as been generated and run the below command
```
$ hadoop jar HW2-assembly-0.1.jar /input /output
```

- The output is generated under /output directory of HDFS. To re-run the program, delete the output file.
```
$hdfs dfs -rm -r /output
```
- The ouptut folder contains of 5 folder each representing output from 5 tasks.Rename the files named part-r-00000 to output.csv to see the output in CSV

###