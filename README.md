# Homework 2
### Description: The objective of this project is to design and implement an instance of the map/reduce computational model on DBLP dataset

## Overview
In this project, we will create Map Reduce programs for parallel processing of the [publically available DBLP dataset](https://dblp.uni-trier.de) that contains entries for various publications at many different venues (e.g., conferences and journals).We perform multiple map/reduce jobs to analyze the data. Below are the tasks performed to get insights into the data.

- Find top ten published authors at each venue.
- Compute the list of authors who published without interruption for N years where 10 <= N.
- Produce the list of publications that contains only one author for each venue.
- Produce the list of publications for each venue that contain the highest number of authors for each of these venues. 
- Compute the list of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors.

## Instructions
Below are the instructions to run the project

### Prerequisites
Required setup to run the program. Different versions of below environment may lead to inconsistencies.
```
jdk-1.8.0_265
Scala v2.13.3 
Hadoop v3.3.0   
Simple build tool (SBT) v1.3.13
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
- Place the input file "dblp.xml" in the input directory
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
After running the above command, a jar file is created under the path target/scala-2.13/ in the project

- Traverse to the folder where the JAR as been generated and run the below command
```
$ hadoop jar <JAR-NAME> /input /output
```
- Example

```
$ hadoop jar HW2-assembly-0.1.jar /input /output
```

- The output is generated under /output directory of HDFS. To re-run the program, delete the output directory before running it again using the below commands.
```
$hdfs dfs -rm -r /output
```
OR

```
 hdfs dfs -rm -r -f /output  
```

- The output folder contains 5 folders each representing output from 5 tasks.Rename the files named part-r-00000 to output.csv to see the output in CSV

### Task and output Description
- Task 1 - Top ten published authors at each venue - 
The program outputs the top ten authors who have published the most at each venue. The output is generated at /output//Task1-AuthorVenue.
The sample output looks like below. The first column shows the Venue and Second column shows Author and the number of publications they have published under each venue.

```
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Kai Goebel 5
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Michael G. Pecht 4
AAAI Fall Symposium: Artificial Intelligence for Prognostics	George J. Vachtsevanos 2
AAAI Fall Symposium: Artificial Intelligence for Prognostics	J. Wesley Hines 2
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Bhaskar Saha 2
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Abhinav Saxena 2
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Michael Baysek 1
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Timothy Stewart 1
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Carl S. Byington 1
AAAI Fall Symposium: Artificial Intelligence for Prognostics	Shannon Mikus 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Victor Raskin 3
AAAI Fall Symposium: Artificial Intelligence of Humor	        J√©r√¥me Urbain 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Kohichi Sayama 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        John Charles Simon 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Dallin D. Oaks 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Pawel Dybala 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Leo Obrst 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        John F. Sowa 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Kenji Araki 1
AAAI Fall Symposium: Artificial Intelligence of Humor	        Jukka M. Toivanen 1

```

- Task 2 - List of authors who published without interruption for N years where 10 <= N
The programs outputs a list of authors who have published consecutively for more than 10 years.
The output can be found at /output/Task2-AuthorNYears.The sample output looks like below. The first column shows the author name and second column shows the number of years they have published without interruption.

```
A Min Tjoa	            31
A-Xing Zhu	            12
A. A. Soliman	        11
A. Agung Julius	        15
A. Alan B. Pritsker	    14
A. Ant Ozok	            11
A. Aydin Alatan 	    27
A. Ben Hamza	        20
A. Benjamin Premkumar	16
A. C. Cem Say	        12
```

- Task 3 -  List of publications with single author under each venue
This task will produce the list of publications that contains only one author under each venue.
The output can be found at /output/Task3-PublicationOneAuthor. The sample looks like below.
First column shows the venue name and the second column lists the titles of publication which contains only one author
```
4WARD Project	                    [A System Overview.	 Security Aspects and Principles.	 How to Manage and Search/Retrieve Information Objects.]																	
50 Years of Artificial Intelligence	[The Dynamic Darwinian Diorama: A Landlocked Archipelago Enhances Epistemology.	 On the Role of AI in the Ongoing Paradigm Shift within the Cognitive Sciences.	 Preliminary Considerations for a Quantitative Theory of Networked Embodied Intelligence.	 A Paradigm Shift in Artificial Intelligence: Why Social Intelligence Matters in the Design and Development of Robots with Human-Like Intelligence.	 The Physical Symbol System Hypothesis: Status and Prospects.	 What Can AI Get from Neuroscience?	 Evolutionary Humanoid Robotics: Past Present and Future.	 How to Build Consciousness into a Robot: The Sensorimotor Approach.	 Curious and Creative Machines.	 Anticipation and Future-Oriented Capabilities in Natural and Artificial Cognition.	 Fifty Years of AI: From Symbols to Embodiment - and Back.	 2006: Celebrating 75 Years of AI - History and Outlook: The Next 25 Years.]								
50 Years of Integer Programming	    [Integer Programming: Methods Uses Computation.	 Semidefinite Relaxations for Integer Programming.	 Matroid Partition.	 Mixed Integer Programming Computation.	 Disjunctive Programming.	 Fifty-Plus Years of Combinatorial Integer Programming.	 Symmetry in Integer Linear Programming.	 Integer Programming and Algorithmic Geometry of Numbers - A tutorial.	 Reducibility Among Combinatorial Problems.	 Lagrangian Relaxation for Integer Programming.	 The Hungarian Method for the Assignment Problem.	 Outline of an Algorithm for Integer Solutions to Linear Programs and An Algorithm for the Mixed Integer Problem.]								
5G World Forum	                    [Feasibility and Challenges of Over-The-Air Testing for 5G Millimeter Wave Devices.	 Blind Carrier Detection for Signals with Unknown Modulation.	 An Overview of Proactive Forensic Solutions and its Applicability to 5G.	 Tractable Scheduling Algorithms for Self-Backhaul in 5G Networks.	 5G-NR Bandwidth Efficient Modulation Options for Efficient Link Operation that are Compatible with mmW Transistor Nonlinearities.	 URLLC Design for Real-Time Control in Wireless Control Systems.	 Autonomous 5G Smallcell Network Deployment and Optimization in Unlicensed Spectrum.	 EIRP TRP Partial TRP and Radiated Immunity For 5G millimeter Wave Device Compliance.	 A Comparison of Scheduling Algorithms for Wireless Access plus X-Haul.	 Engineering the 5G Environment.	 Initial Study on the Architecture of Field Observation in 5G Era.	 Private 5G Networks for Vertical Industries: Deployment and Operation Models.]								
```

- Task 4 - List of publications which contains the highest number of authors for each venues
The programs outputs the list of publication for each venue that contains highest number of authors for each of these venue.
The output can be found at /output/Task4-AuthorPublicationMapper.  The first column shows the venue followed by list of publication for each venue which contain highest number of authors under that venue

```
25 Years of Model Checking	                                ["New Challenges in Model Checking."]				
25th Anniversary of INRIA	                                ["Control Software for Virtual-Circuit Switches: Call Processing."]				
35 Years of Fuzzy Set Theory	                            ["Fuzzy Techniques in Image Processing at Ghent University: Summary of a 12-Year Journey."]				
3D Flash Memories	                                        ["RRAM Cross-Point Arrays."]				
3D Image Processing Measurement (3DIPM) and Applications	["Towards automated high resolution 3D scanning of large surfaces for cultural heritage documentation."]				
3D Imaging Analysis and Applications	                    ["Feature-Based Methods in 3D Shape Analysis."	 "Introduction."	 "3D Medical Imaging."]		
3D Integration for NoC-based SoC Architectures	            ["Influence of Stacked 3D Memory/Cache Architectures on GPUs."]				
3D Multiscale Physiological Human	                        ["Coupled Biomechanical Modeling of the Face Jaw Skull Tongue and Hyoid Bone."	 "Clinical Gait Analysis and Musculoskeletal Modeling."]			
3D Research Challenges in Cultural Heritage	                ["Enrichment and Preservation of Architectural Knowledge."]				
```

- Task  5 - List of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors.
The programs output the list of authors who have published with highest number of co-authors and the list of authors who have published with no co-authors or least number of co-authors.
The output can be found at /output/Task5-MostCoAuthors.
This file contains the output for both Most co-authors and no co-authors.
The output sample looks like below. The first column contains the author's name and second column contains publications published by him/her.
```
Top 100 authors with most co-authors
Wei Li	    3642
Yang Liu	3436
Wei Wang	3407
Wei Zhang	3337
Lei Zhang	3318
Yu Zhang	3224
Lei Wang	3039
Li Zhang	2678
Xin Wang	2623
Jing Wang	2571
Yan Li	    2560

Top 100 authors with no co-authors	

C. P. Gill	        0
Robert J. Betts	    0
Christopher Mohri	0
Hartmut Michels	    0
Angus Dunn	        0
Martin Fl√∂ck	    0
John H. Picklo	    0
Kendall Bartsch	    0
Theeratorn Lersilp	0
Rooholah Majdodin	0
Gregory E. Feldkamp	0
Gabor Laszlo	    0
K. Tchou	        0
```
### Implementation on AWS EMR
The project has been deployed on AWS Elastic Map Reduce. The steps to setup the cluster and execute Map Reduce program can be 
found at [youtube](https://youtu.be/pCzyeffHixw).
Alternate drive [link](https://drive.google.com/drive/folders/1lzqiZF-lXn1Ro1uOoPmmS2TSYDC46hjt?usp=sharing), in case the above link doesn't work
[youtube-480p](https://youtu.be/pXmjgHDRYd0)
