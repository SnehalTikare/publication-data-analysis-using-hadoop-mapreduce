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
Below are the prerequisites required to run the program
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

### Task Description
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
AAAI Fall Symposium: Artificial Intelligence of Humor	Victor Raskin 3
AAAI Fall Symposium: Artificial Intelligence of Humor	J√©r√¥me Urbain 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Kohichi Sayama 1
AAAI Fall Symposium: Artificial Intelligence of Humor	John Charles Simon 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Dallin D. Oaks 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Pawel Dybala 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Leo Obrst 1
AAAI Fall Symposium: Artificial Intelligence of Humor	John F. Sowa 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Kenji Araki 1
AAAI Fall Symposium: Artificial Intelligence of Humor	Jukka M. Toivanen 1

```

- Task 2 - List of authors who published without interruption for N years where 10 <= N
The programs outputs a list of authors who have published consecutively for more than 10 years.
The output can be found at /output/Task2-AuthorNYears.The sample output looks below. The first column shows the author name and second column shows the number of years they have published without interruption.

```
A Min Tjoa	31
A-Xing Zhu	12
A. A. Soliman	11
A. Agung Julius	15
A. Alan B. Pritsker	14
A. Ant Ozok	11
A. Aydin Alatan	27
A. Ben Hamza	20
A. Benjamin Premkumar	16
A. C. Cem Say	12
```

- Task 3 -  List of publications with single author under each venue
This task will produce the list of publications that contains only one author under each venue.
The output can be found at /output/Task3-PublicationOneAuthor. The sample looks like below.

```
#MSM	[Information Theoretic Tools for Social Media.	 Computational Social Science and Microblogs - The Good the Bad and the Ugly.	 ACE: A Concept Extraction Approach using Linked Open Data.	 A New ANEW: Evaluation of a Word List for Sentiment Analysis in Microblogs.	 Unsupervised Information Extraction using BabelNet and DBpedia.]					
#Microposts	[Studying the Role of Elites in U.S. Political Twitter Debates.]									
(KNOW@LOD/CoDeS)@ESWC	[Finding and Avoiding Bugs in Enterprise Ontologies.	 Not-So-Linked Solution to the Linked Data Mining Challenge 2016.]								
*SEM@ACL	[Natural Solution to FraCaS Entailment Problems.]									
*SEM@COLING	[Learning the Peculiar Value of Actions.	 Identifying semantic relations in a specialized corpus through distributional analysis of a cooccurrence tensor.]								
```

- Task 4 - List of publications with contain the highest number of authors for each venues
The programs outputs the list of publication for each venue that contains highest number of authors for each of these venue.
The output can be found at /output/Task4-AuthorPublicationMapper.  The first column shows the venue followed by list of publication for each venue which contain highest number of authors under that venue

```
25 Years of Model Checking	["New Challenges in Model Checking."]				
25th Anniversary of INRIA	["Control Software for Virtual-Circuit Switches: Call Processing."]				
35 Years of Fuzzy Set Theory	["Fuzzy Techniques in Image Processing at Ghent University: Summary of a 12-Year Journey."]				
3D Flash Memories	["RRAM Cross-Point Arrays."]				The
3D Image Processing Measurement (3DIPM) and Applications	["Towards automated high resolution 3D scanning of large surfaces for cultural heritage documentation."]				
3D Imaging Analysis and Applications	["Feature-Based Methods in 3D Shape Analysis."	 "Introduction."	 "3D Medical Imaging."]		
3D Integration for NoC-based SoC Architectures	["Influence of Stacked 3D Memory/Cache Architectures on GPUs."]				
3D Multiscale Physiological Human	["Coupled Biomechanical Modeling of the Face Jaw Skull Tongue and Hyoid Bone."	 "Clinical Gait Analysis and Musculoskeletal Modeling."]			
3D Research Challenges in Cultural Heritage	["Enrichment and Preservation of Architectural Knowledge."]				
```

- Task  5 - List of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors.
The programs output the list of authors who have published with highest number of co-authors and the list of authors who have published with no co-authors or least number of co-authors.
The output can be found at /output/Task5-MostCoAuthors.
This file contains the output for both Most co-authors and no co-authors.
The output sample looks like below. The first column contains the author's name and second column contains publications published by him/her.
```
Top 100 authors with most co-authors
Wei Li	3642
Yang Liu	3436
Wei Wang	3407
Wei Zhang	3337
Lei Zhang	3318
Yu Zhang	3224
Lei Wang	3039
Li Zhang	2678
Xin Wang	2623
Jing Wang	2571
Yan Li	2560

Top 100 authors with no co-authors	

C. P. Gill	0
Robert J. Betts	0
Christopher Mohri	0
Hartmut Michels	0
Angus Dunn	0
Martin Fl√∂ck	0
John H. Picklo	0
Kendall Bartsch	0
Theeratorn Lersilp	0
Rooholah Majdodin	0
Gregory E. Feldkamp	0
Gabor Laszlo	0
K. Tchou	0
```

