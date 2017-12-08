Java project created in Eclipse Mars 2 Release (4.5.2); Windows 10

Technologies:

Java jdk1.8.0_144 (64 bits) ==> Mandatory
Maven 4.0
Hadoop 2.7.4

1)GromenauerHadoopIsolate ==> MapReduce Test Project.

Execution:

(In hdfs; we create the next folder tree
/test
//input
///input_2
//output
)

1)start-all

2)(Optional) hadoop dfsadmin -safemode leave (safemode = OFF)

3)(Optional) hdfs dfs -rm -r -f /test/output/*

4)hadoop jar /test/test.jar Main /test/input/input_2 /test/output 1
(test.jar is executed from c:/test/test.jar)

5)stop-all

HADOOP INSTALATION WINDOWS
======================================================================

Hadoop instalation: http://toodey.com/2015/08/10/hadoop-installation-on-windows-without-cygwin-in-10-mints/
Hadoop execution: http://www.srccodes.com/p/article/45/run-hadoop-wordcount-mapreduce-example-windows

(
Define JAVA_PATH (Java SDK root path) HADOOP_PATH (HADOOP root path) in windows environment variables; 
Include bin/sbin HADOOP path in PATH windows environment variables
Modify script start_all set JAVA to JAVA_PATH
)

Test:

hadoop jar /workspaces/workEclipse/workMarsGromenauer/server/hadoop-2.7.4/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.4.jar wordcount /test/input/input_2 /test/output

Nodes HADOOP:
http://localhost:8088
http://localhost:50070

PROBLEMS:

Cannot delete /tmp/hadoop-yarn/stagi ==> temp file blocked safety flag.

ExitCode -1000 ==> execute all the script as admin

bindException (task using 50010 port) ==> kill process that use this port
https://stackoverflow.com/questions/39632667/how-to-kill-a-currently-using-port-on-localhost-in-windows

Problem auth filter enabled (change localhost to 127.0.0.1; core-site.xml; hdfs-site.xml)
https://stackoverflow.com/questions/27822166/failed-to-be-authorized-from-yarn-resource-manager-webapp-under-kerberos

Stuck Map 0% Reduce %0 (Play yarn-site.xml; mapred-site.xml; set space memory;
and num cores. For a high capacity computer comment space memory and cores)

Optimization:
https://es.hortonworks.com/blog/how-to-plan-and-configure-yarn-in-hdp-2-0/
https://stackoverflow.com/questions/18525354/map-reduce-job-getting-stuck-at-map-0-reduce-0
https://stackoverflow.com/questions/16713011/hadoop-namenode-is-not-starting-up

mapreduce Uber mode:
https://stackoverflow.com/questions/30284237/what-is-the-purpose-of-uber-mode-in-hadoop
https://github.com/sayden/hadoop-summary/blob/master/05-How-mapreduce-works.md
https://www.cloudera.com/documentation/enterprise/5-3-x/topics/cdh_ig_yarn_tuning.html
http://networkgeekstuff.com/networking/tutorial-for-small-hadoop-cloud-cluster-lab-using-virtual-machines-and-compilingrunning-first-hello-world-map-reduce-example-project/


For Hadoop 1.x: (Windows Permissions)

https://github.com/congainc/patch-hadoop_7682-1.0.x-win

======================================================================

hdfs commands:

hdfs dfs mkdir /input (create hdfs input folder)
hdfs dfs -copyFromLocal c:/workspaces/workEclipse/workMarsGromenauer/GromenauerHadoop/input_2 /input (copy data from local)
hdfs dfs -ls /input (dir hdfs input folder)
hdfs dfs -cat /input/prueba0.txt (print content of hdfs /input/prueba0.txt )

hdfs dfs -rm /test/output (delete hdfs file/folder )
hdfs dfs -rm -r -f /test/output/* (delete hdfs folder recursively)

hdfs dfs -expunge (delete cache data)

jps (see hadoop created)